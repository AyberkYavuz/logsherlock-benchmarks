#!/usr/bin/env python3
"""Generate Spring Boot + Logback benchmark log datasets.

The script owns the whole lifecycle of one dataset: it starts the Spring Boot
benchmark application in the requested log format, waits until the HTTP service
answers, triggers a scenario through ``POST /api/benchmark/run``, captures the
business log records the application wrote to stdout, and stops the process.

Every scenario runs in a fresh JVM so that seeded inventory and the deterministic
identifier counters always start from the same state.

Examples
--------
    python3 scripts/generate_springboot_logs.py --format text --scenario NORMAL
    python3 scripts/generate_springboot_logs.py --format json --scenario NORMAL
    python3 scripts/generate_springboot_logs.py --format json --scenario ALL
"""

from __future__ import annotations

import argparse
import json
import os
import re
import shutil
import signal
import socket
import subprocess
import sys
import threading
import time
import urllib.error
import urllib.request
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
APP_DIR = REPO_ROOT / "apps" / "springboot-logback"
DEFAULT_OUTPUT_DIR = REPO_ROOT / "datasets" / "springboot-logback"

SCENARIOS = [
    "NORMAL",
    "INVALID_ORDER",
    "OUT_OF_STOCK",
    "PAYMENT_DECLINED",
    "SHIPPING_DELAY",
]

FORMATS = ["text", "json"]

# Every business record is emitted through BenchmarkLoggerImpl; framework and
# startup records carry a different logger. This is the only marker needed to
# separate the benchmark stream from Spring Boot noise, and it is identical in
# both log formats because the JSON formatter writes the same logger value the
# `%logger{0}` pattern conversion word produces.
BUSINESS_LOGGER = "BenchmarkLoggerImpl"

# Structured fields of the benchmark vocabulary, in the order the plaintext
# pattern prints them.
STRUCTURED_KEYS = [
    "application",
    "environment",
    "schemaVersion",
    "scenario",
    "reqId",
    "traceId",
    "orderId",
    "customerId",
    "productId",
    "paymentId",
    "shipmentId",
    "service",
    "component",
]

TEXT_RECORD_PATTERN = re.compile(
    r"^(?P<timestamp>\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}) "
    r"(?P<level>[A-Z]+)\s+"
    r"\[(?P<thread>[^\]]*)\] "
    r"(?P<logger>\S+) "
    + " ".join(rf"{key}=(?P<{key}>\S*)" for key in STRUCTURED_KEYS)
    + r" (?P<message>.*)$"
)


class GenerationError(RuntimeError):
    """Raised when a dataset could not be generated."""


# ---------------------------------------------------------------------------
# Log record parsing
# ---------------------------------------------------------------------------


def parse_text_record(line):
    """Parses one plaintext log line, or returns ``None`` if it is not a record."""
    match = TEXT_RECORD_PATTERN.match(line)
    if match is None:
        return None
    record = match.groupdict()
    # The pattern layout prints an absent MDC entry as an empty value; the JSON
    # formatter omits the field. Normalise to "absent" so both are comparable.
    return {key: value for key, value in record.items() if value != ""}


def parse_json_record(line):
    """Parses one JSON log line, or returns ``None`` if it is not a JSON object."""
    try:
        record = json.loads(line)
    except json.JSONDecodeError:
        return None
    return record if isinstance(record, dict) else None


def parse_record(line, log_format):
    return parse_text_record(line) if log_format == "text" else parse_json_record(line)


def is_business_record(record):
    return record is not None and record.get("logger") == BUSINESS_LOGGER


def select_business_lines(lines, log_format):
    """Returns the raw lines that hold a benchmark business record, with their parse."""
    selected = []
    for line in lines:
        record = parse_record(line, log_format)
        if is_business_record(record):
            selected.append((line, record))
    return selected


# ---------------------------------------------------------------------------
# Application process
# ---------------------------------------------------------------------------


def find_jar(rebuild, skip_build):
    """Returns the executable jar, building it with Maven when necessary."""
    jars = [
        jar
        for jar in APP_DIR.glob("target/springboot-logback-*.jar")
        if not jar.name.endswith(".original")
    ]
    if jars and not rebuild:
        return jars[0]
    if skip_build:
        raise GenerationError(
            f"No executable jar under {APP_DIR / 'target'} and --skip-build was given. "
            "Run 'mvn -f apps/springboot-logback/pom.xml package' first."
        )

    mvn = shutil.which("mvn")
    if mvn is None:
        raise GenerationError("Maven ('mvn') is not on PATH; cannot build the application.")

    print(f"[build] {mvn} package -DskipTests ({APP_DIR})", flush=True)
    result = subprocess.run(
        [mvn, "-B", "-q", "package", "-DskipTests"],
        cwd=APP_DIR,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
    )
    if result.returncode != 0:
        raise GenerationError(f"Maven build failed:\n{result.stdout}")

    jars = [
        jar
        for jar in APP_DIR.glob("target/springboot-logback-*.jar")
        if not jar.name.endswith(".original")
    ]
    if not jars:
        raise GenerationError("Maven build produced no executable jar.")
    return jars[0]


def require_free_port(port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as probe:
        probe.settimeout(1.0)
        if probe.connect_ex(("127.0.0.1", port)) == 0:
            raise GenerationError(
                f"Port {port} is already in use. Stop the process using it or pass --port."
            )


class BenchmarkApplication:
    """Runs one Spring Boot process and collects everything it writes to stdout."""

    def __init__(self, jar, log_format, port, java):
        self.jar = jar
        self.log_format = log_format
        self.port = port
        self.java = java
        self.process = None
        self.lines = []
        self._lock = threading.Lock()
        self._reader = None

    def __enter__(self):
        self.start()
        return self

    def __exit__(self, exc_type, exc, traceback):
        self.stop()
        return False

    def start(self):
        env = dict(os.environ)
        # Runtime log format selection; the Logback configuration reads it.
        env["LOG_FORMAT"] = self.log_format
        command = [
            self.java,
            "-jar",
            str(self.jar),
            f"--server.port={self.port}",
            # Skip the startup smoke workflow so the dataset holds only the
            # scenario requested over HTTP, run against untouched seed data.
            "--benchmark.smoke-test.enabled=false",
        ]
        print(f"[start] LOG_FORMAT={self.log_format} {' '.join(command)}", flush=True)
        self.process = subprocess.Popen(
            command,
            cwd=APP_DIR,
            env=env,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            bufsize=1,
            start_new_session=True,
        )
        self._reader = threading.Thread(target=self._read_output, daemon=True)
        self._reader.start()

    def _read_output(self):
        for line in self.process.stdout:
            with self._lock:
                self.lines.append(line.rstrip("\n"))

    def snapshot(self):
        with self._lock:
            return list(self.lines)

    def output_tail(self, count=40):
        return "\n".join(self.snapshot()[-count:])

    def wait_until_ready(self, timeout):
        """Polls the read-only product endpoint until the application answers."""
        url = f"http://127.0.0.1:{self.port}/api/products"
        deadline = time.monotonic() + timeout
        while time.monotonic() < deadline:
            if self.process.poll() is not None:
                raise GenerationError(
                    "Application exited during startup with code "
                    f"{self.process.returncode}:\n{self.output_tail()}"
                )
            try:
                with urllib.request.urlopen(url, timeout=2) as response:
                    if response.status == 200:
                        print(f"[ready] {url}", flush=True)
                        return
            except (urllib.error.URLError, OSError, TimeoutError):
                pass
            time.sleep(0.25)
        raise GenerationError(
            f"Application was not ready on {url} within {timeout}s:\n{self.output_tail()}"
        )

    def run_scenario(self, scenario, timeout):
        """Triggers one scenario through the Phase 7 HTTP endpoint."""
        url = f"http://127.0.0.1:{self.port}/api/benchmark/run"
        body = json.dumps({"scenario": scenario}).encode("utf-8")
        request = urllib.request.Request(
            url, data=body, headers={"Content-Type": "application/json"}, method="POST"
        )
        try:
            with urllib.request.urlopen(request, timeout=timeout) as response:
                payload = json.loads(response.read().decode("utf-8"))
                status = response.status
        except urllib.error.HTTPError as error:
            raise GenerationError(
                f"POST {url} failed with HTTP {error.code}: {error.read().decode('utf-8', 'replace')}"
            ) from error
        except (urllib.error.URLError, OSError, TimeoutError) as error:
            raise GenerationError(f"POST {url} failed: {error}") from error

        if status != 200 or payload.get("status") != "completed":
            raise GenerationError(f"Unexpected benchmark run response: HTTP {status} {payload}")
        if payload.get("scenario") != scenario:
            raise GenerationError(
                f"Requested scenario {scenario} but the application ran {payload.get('scenario')}"
            )
        print(f"[run]   POST /api/benchmark/run -> {payload}", flush=True)

    def drain_output(self, quiet_period=0.5, timeout=5.0):
        """Waits until the captured output stops growing, so no record is lost."""
        deadline = time.monotonic() + timeout
        last_count = len(self.snapshot())
        stable_since = time.monotonic()
        while time.monotonic() < deadline:
            time.sleep(0.05)
            current = len(self.snapshot())
            if current != last_count:
                last_count = current
                stable_since = time.monotonic()
            elif time.monotonic() - stable_since >= quiet_period:
                return

    def stop(self, timeout=20):
        """Terminates the process group, escalating to SIGKILL if needed."""
        if self.process is None:
            return
        if self.process.poll() is None:
            try:
                os.killpg(os.getpgid(self.process.pid), signal.SIGTERM)
            except (ProcessLookupError, PermissionError):
                self.process.terminate()
            try:
                self.process.wait(timeout=timeout)
            except subprocess.TimeoutExpired:
                try:
                    os.killpg(os.getpgid(self.process.pid), signal.SIGKILL)
                except (ProcessLookupError, PermissionError):
                    self.process.kill()
                self.process.wait(timeout=timeout)
        if self._reader is not None:
            self._reader.join(timeout=5)
        if self.process.stdout is not None:
            self.process.stdout.close()
        print(f"[stop]  exit code {self.process.returncode}", flush=True)


# ---------------------------------------------------------------------------
# Dataset generation
# ---------------------------------------------------------------------------


def verify_records(scenario, log_format, records):
    """Checks that the captured stream is one complete, correct scenario run."""
    if not records:
        raise GenerationError(
            f"No {BUSINESS_LOGGER} records captured for scenario {scenario} ({log_format})."
        )

    scenarios = {record.get("scenario") for record in records}
    if scenarios != {scenario}:
        raise GenerationError(
            f"Expected only scenario {scenario} in the captured records, found {sorted(scenarios)}."
        )

    request_ids = {record.get("reqId") for record in records}
    if len(request_ids) != 1:
        raise GenerationError(
            f"Expected a single request id for one scenario run, found {sorted(request_ids)}. "
            "State from an earlier run leaked into this process."
        )

    for key in ("timestamp", "level", "thread", "logger", "message"):
        missing = [record for record in records if not record.get(key)]
        if missing:
            raise GenerationError(f"{len(missing)} captured records carry no '{key}'.")

    return request_ids.pop()


def verify_written_file(path, log_format, expected_count):
    """Re-reads the generated file and validates it independently of the run."""
    lines = path.read_text(encoding="utf-8").splitlines()
    if len(lines) != expected_count:
        raise GenerationError(f"{path} holds {len(lines)} lines, expected {expected_count}.")
    for number, line in enumerate(lines, start=1):
        record = parse_record(line, log_format)
        if not is_business_record(record):
            raise GenerationError(f"{path}:{number} is not a benchmark business record.")


def generate(scenario, log_format, jar, args):
    """Generates one dataset file and returns the parsed records it holds."""
    print(f"\n=== {scenario} / {log_format} ===", flush=True)
    require_free_port(args.port)

    with BenchmarkApplication(jar, log_format, args.port, args.java) as application:
        application.wait_until_ready(args.startup_timeout)
        application.run_scenario(scenario, args.request_timeout)
        application.drain_output()
        captured = application.snapshot()

    business = select_business_lines(captured, log_format)
    records = [record for _, record in business]
    request_id = verify_records(scenario, log_format, records)

    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    output_path = output_dir / f"{scenario}.{log_format}.log"
    output_path.write_text("\n".join(line for line, _ in business) + "\n", encoding="utf-8")
    verify_written_file(output_path, log_format, len(business))

    if args.keep_raw:
        raw_path = output_dir / f"{scenario}.{log_format}.raw.log"
        raw_path.write_text("\n".join(captured) + "\n", encoding="utf-8")
        print(f"[raw]   {raw_path.relative_to(REPO_ROOT)}", flush=True)

    print(
        f"[write] {output_path.relative_to(REPO_ROOT)} "
        f"({len(business)} records, reqId={request_id})",
        flush=True,
    )
    return records


def build_parser():
    parser = argparse.ArgumentParser(
        description="Generate Spring Boot + Logback benchmark log datasets.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__.split("Examples\n--------\n", 1)[1],
    )
    parser.add_argument(
        "--format",
        dest="formats",
        nargs="+",
        choices=FORMATS + ["all"],
        default=["text"],
        help="log format(s) to generate; 'all' expands to text and json (default: text)",
    )
    parser.add_argument(
        "--scenario",
        dest="scenarios",
        nargs="+",
        choices=SCENARIOS + ["ALL"],
        default=["NORMAL"],
        help="scenario(s) to run; 'ALL' expands to every scenario (default: NORMAL)",
    )
    parser.add_argument(
        "--output-dir",
        default=str(DEFAULT_OUTPUT_DIR),
        help=f"directory the datasets are written to (default: {DEFAULT_OUTPUT_DIR})",
    )
    parser.add_argument("--port", type=int, default=8080, help="HTTP port (default: 8080)")
    parser.add_argument("--java", default="java", help="java executable (default: java)")
    parser.add_argument(
        "--startup-timeout",
        type=float,
        default=90.0,
        help="seconds to wait for the HTTP service to become ready (default: 90)",
    )
    parser.add_argument(
        "--request-timeout",
        type=float,
        default=60.0,
        help="seconds to wait for a benchmark run to finish (default: 60)",
    )
    parser.add_argument(
        "--rebuild", action="store_true", help="rebuild the application jar before generating"
    )
    parser.add_argument(
        "--skip-build", action="store_true", help="fail instead of building a missing jar"
    )
    parser.add_argument(
        "--keep-raw",
        action="store_true",
        help="also write the complete process output next to each dataset",
    )
    return parser


def expand(values, all_token, everything):
    return list(everything) if all_token in values else list(dict.fromkeys(values))


def main(argv=None):
    args = build_parser().parse_args(argv)
    scenarios = expand(args.scenarios, "ALL", SCENARIOS)
    formats = expand(args.formats, "all", FORMATS)

    try:
        jar = find_jar(args.rebuild, args.skip_build)
        print(f"[jar]   {jar.relative_to(REPO_ROOT)}", flush=True)
        for log_format in formats:
            for scenario in scenarios:
                generate(scenario, log_format, jar, args)
    except GenerationError as error:
        print(f"\nERROR: {error}", file=sys.stderr)
        return 1
    except KeyboardInterrupt:
        print("\nInterrupted.", file=sys.stderr)
        return 130

    print(
        f"\nGenerated {len(scenarios) * len(formats)} dataset(s) in {args.output_dir}",
        flush=True,
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
