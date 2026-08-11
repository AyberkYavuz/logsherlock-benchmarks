#!/usr/bin/env python3

import argparse
import json
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path


SCENARIOS = [
    "NORMAL",
    "INVALID_ORDER",
    "OUT_OF_STOCK",
    "PAYMENT_DECLINED",
    "SHIPPING_DELAY",
]


def http_post(base_url: str, scenario: str, timeout: float) -> dict:
    url = f"{base_url}/api/benchmark/run"
    payload = json.dumps({"scenario": scenario}).encode("utf-8")

    request = urllib.request.Request(
        url,
        data=payload,
        headers={"Content-Type": "application/json"},
        method="POST",
    )

    with urllib.request.urlopen(request, timeout=timeout) as response:
        body = response.read().decode("utf-8")
        return json.loads(body)


def wait_for_server(base_url: str, timeout: float) -> None:
    deadline = time.monotonic() + timeout
    url = f"{base_url}/api/products"

    while time.monotonic() < deadline:
        try:
            with urllib.request.urlopen(url, timeout=2) as response:
                if response.status == 200:
                    return
        except (urllib.error.URLError, TimeoutError):
            pass

        time.sleep(0.1)

    raise RuntimeError(f"Server did not become ready: {base_url}")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate large Spring Boot log datasets by driving the HTTP benchmark API."
    )

    parser.add_argument(
        "--format",
        choices=["text", "json"],
        required=True,
        help="Expected Java log format.",
    )

    parser.add_argument(
        "--runs",
        type=int,
        default=1000,
        help="Number of benchmark workflows to execute.",
    )

    parser.add_argument(
        "--scenario",
        choices=SCENARIOS + ["MIXED"],
        default="MIXED",
        help="Scenario to execute. MIXED selects scenarios deterministically.",
    )

    parser.add_argument(
        "--output",
        required=True,
        help="Output dataset path.",
    )

    parser.add_argument(
        "--raw-log",
        help="Path to the captured Java stdout/stderr log.",
    )

    parser.add_argument(
        "--base-url",
        default="http://127.0.0.1:8080",
        help="Spring Boot server base URL.",
    )

    parser.add_argument(
        "--seed",
        type=int,
        default=42,
        help="Seed used for deterministic MIXED scenario selection.",
    )

    parser.add_argument(
        "--timeout",
        type=float,
        default=10.0,
        help="HTTP request timeout in seconds.",
    )

    parser.add_argument(
        "--progress-every",
        type=int,
        default=100,
        help="Print progress every N runs.",
    )

    parser.add_argument(
        "--delay",
        type=float,
        default=0.0,
        help="Optional delay between requests in seconds.",
    )

    return parser.parse_args()


def choose_scenario(index: int, scenario: str, seed: int) -> str:
    if scenario != "MIXED":
        return scenario

    # Deterministic pseudo-random selection without external dependencies.
    value = (index * 1103515245 + seed * 12345) & 0x7FFFFFFF
    return SCENARIOS[value % len(SCENARIOS)]


def filter_json_records(raw_log: Path, output: Path) -> tuple[int, int]:
    """
    Keep every line from the Java stdout that is a valid JSON object.

    This intentionally removes non-JSON startup noise such as the
    Spring Boot ASCII banner while preserving all valid JSON log records,
    including framework and BenchmarkLoggerImpl records.
    """
    total_lines = 0
    kept_records = 0

    output.parent.mkdir(parents=True, exist_ok=True)

    with raw_log.open("r", encoding="utf-8", errors="replace") as source, \
            output.open("w", encoding="utf-8") as destination:

        for line in source:
            total_lines += 1
            stripped = line.strip()

            if not stripped:
                continue

            try:
                record = json.loads(stripped)
            except json.JSONDecodeError:
                continue

            if not isinstance(record, dict):
                continue

            destination.write(
                json.dumps(record, ensure_ascii=False, separators=(",", ":"))
                + "\n"
            )
            kept_records += 1

    return total_lines, kept_records


def copy_text_records(raw_log: Path, output: Path) -> tuple[int, int]:
    """
    For text mode, preserve the captured stdout.

    Text logs are intentionally not JSON-filtered.
    """
    total_lines = 0
    written_lines = 0

    output.parent.mkdir(parents=True, exist_ok=True)

    with raw_log.open("r", encoding="utf-8", errors="replace") as source, \
            output.open("w", encoding="utf-8") as destination:

        for line in source:
            total_lines += 1
            destination.write(line)
            written_lines += 1

    return total_lines, written_lines


def main() -> None:
    args = parse_args()

    if args.runs <= 0:
        raise SystemExit("--runs must be greater than zero")

    if args.progress_every <= 0:
        raise SystemExit("--progress-every must be greater than zero")

    output = Path(args.output)
    raw_log = Path(args.raw_log) if args.raw_log else None

    print(f"[server] {args.base_url}")
    print(f"[format] {args.format}")
    print(f"[runs]   {args.runs}")
    print(f"[scenario] {args.scenario}")
    print(f"[output] {output}")

    print("[ready] waiting for Spring Boot server...")
    wait_for_server(args.base_url, args.timeout)
    print("[ready] server is responding")

    started = time.monotonic()
    scenario_counts = {scenario: 0 for scenario in SCENARIOS}

    with raw_log.open("w", encoding="utf-8") as raw:
        for index in range(args.runs):
            scenario = choose_scenario(index, args.scenario, args.seed)
            scenario_counts[scenario] += 1

            try:
                response = http_post(
                    args.base_url,
                    scenario,
                    args.timeout,
                )
            except Exception as exc:
                print(
                    f"\nERROR: run {index + 1} failed "
                    f"for scenario {scenario}: {exc}",
                    file=sys.stderr,
                )
                raise SystemExit(1)

            if response.get("status") != "completed":
                print(
                    f"\nERROR: unexpected response for run {index + 1}: "
                    f"{response}",
                    file=sys.stderr,
                )
                raise SystemExit(1)

            # The server owns the log stream. The request itself is only
            # the trigger; stdout is captured separately after generation.

            if args.progress_every and (
                (index + 1) % args.progress_every == 0
            ):
                elapsed = time.monotonic() - started
                rate = (index + 1) / elapsed if elapsed else 0
                print(
                    f"[progress] {index + 1}/{args.runs} runs "
                    f"({rate:.1f} runs/sec)"
                )

            if args.delay:
                time.sleep(args.delay)

    elapsed = time.monotonic() - started

    print()
    print("[complete]")
    print(f"runs:       {args.runs}")
    print(f"elapsed:    {elapsed:.2f}s")

    if raw_log is None:
        raise SystemExit(
            "--raw-log is required when collecting logs from an externally started Java server."
        )

    if not raw_log.exists():
        raise SystemExit(f"Raw log does not exist: {raw_log}")

    if args.format == "json":
        total, kept = filter_json_records(raw_log, output)
        print(f"raw lines:  {total}")
        print(f"records:    {kept}")
    else:
        total, kept = copy_text_records(raw_log, output)
        print(f"raw lines:  {total}")
        print(f"records:    {kept}")

    print("scenarios:")
    for scenario in SCENARIOS:
        print(f"{scenario}: {scenario_counts[scenario]}")

    print(f"output:     {output}")


if __name__ == "__main__":
    main()
