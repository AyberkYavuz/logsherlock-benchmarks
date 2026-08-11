# Spring Boot + Logback E-Commerce Application

## Overview

The Spring Boot application simulates a deterministic e-commerce order workflow (order intake, inventory reservation, payment authorization, shipping) for the LogSherlock benchmark suite. Every business step emits a structured log record through `BenchmarkLogger`, so a scenario always produces the same sequence of log events.

The application can emit those records in **two log formats**:

| `LOG_FORMAT` | Output |
| ------------ | ------ |
| `text` (default) | Human-readable Logback pattern layout, one line per record. |
| `json` | One JSON object per line (JSONL). |

Both formats carry exactly the same information — only the serialization differs. The business workflow is identical in either mode.

---

# Selecting the Log Format

The format is chosen at startup and requires no source change and no rebuild. `logback-spring.xml` reads `LOG_FORMAT` from the JVM system properties first and from the OS environment otherwise, then includes the matching console appender definition (`logback/console-appender-text.xml` or `logback/console-appender-json.xml`).

Environment variable:

```bash
LOG_FORMAT=json java -jar apps/springboot-logback/target/springboot-logback-0.0.1-SNAPSHOT.jar
```

JVM system property:

```bash
java -DLOG_FORMAT=json -jar apps/springboot-logback/target/springboot-logback-0.0.1-SNAPSHOT.jar
```

If `LOG_FORMAT` is unset the application logs plaintext, which is the pre-existing behaviour. Spring Boot's own startup logs follow the selected format as well; benchmark business records are always distinguishable by their logger name, `BenchmarkLoggerImpl`.

## Log Record Fields

The same vocabulary is emitted in both formats. In `text` mode a field with no value is printed empty (`orderId=`); in `json` mode it is omitted from the object.

| Field | Text | JSON |
| ----- | ---- | ---- |
| timestamp | `2026-01-01T10:00:00.000` (leading token) | `"timestamp"` |
| level | `INFO ` | `"level"` |
| thread | `[http-nio-8080-exec-1]` | `"thread"` |
| logger | `BenchmarkLoggerImpl` | `"logger"` |
| message | trailing free text | `"message"` |
| application, environment, schemaVersion | `key=value` pairs | object members |
| scenario, reqId, traceId | `key=value` pairs | object members |
| orderId, customerId, productId, paymentId, shipmentId | `key=value` pairs | object members |
| service, component | `key=value` pairs | object members |

Example, text:

```
2026-01-01T10:00:00.000 WARN  [http-nio-8080-exec-2] BenchmarkLoggerImpl application=logsherlock-order-service environment=benchmark schemaVersion=1 scenario=OUT_OF_STOCK reqId=REQ-1001 traceId=TRACE-1001 orderId=ORDER-5001 customerId=CUSTOMER-48 productId=PRODUCT-19 paymentId= shipmentId= service=INVENTORY component=STORE Insufficient stock for PRODUCT-19: requested 101, available 100
```

Example, JSON (single line, wrapped here for readability):

```json
{"timestamp":"2026-01-01T10:00:00.000","level":"WARN","thread":"http-nio-8080-exec-2",
 "logger":"BenchmarkLoggerImpl","message":"Insufficient stock for PRODUCT-19: requested 101, available 100",
 "application":"logsherlock-order-service","environment":"benchmark","schemaVersion":"1",
 "scenario":"OUT_OF_STOCK","reqId":"REQ-1001","traceId":"TRACE-1001","orderId":"ORDER-5001",
 "customerId":"CUSTOMER-48","productId":"PRODUCT-19","service":"INVENTORY","component":"STORE"}
```

---

# Running the Application

Prerequisites: JDK 21 and Maven.

Build:

```bash
mvn -f apps/springboot-logback/pom.xml package
```

Start:

```bash
java -jar apps/springboot-logback/target/springboot-logback-0.0.1-SNAPSHOT.jar
```

The application starts on:

```
http://localhost:8080
```

## HTTP API

Read-only endpoints:

```
GET /api/products      GET /api/products/{productId}
GET /api/orders        GET /api/orders/{orderId}
GET /api/payments      GET /api/payments/{paymentId}
GET /api/shipments     GET /api/shipments/{shipmentId}
```

Benchmark execution:

```bash
curl -X POST http://localhost:8080/api/benchmark/run \
-H "Content-Type: application/json" \
-d '{"scenario":"NORMAL"}'
```

Expected response:

```json
{"scenario":"NORMAL","status":"completed"}
```

---

# Dataset Generation

`scripts/generate_springboot_logs.py` owns the whole lifecycle: it builds the jar if needed, starts the application in the requested log format, polls `GET /api/products` until the service answers, triggers the scenario through `POST /api/benchmark/run`, captures the business log records, writes the dataset and stops the process. Each scenario runs in a **fresh JVM**, so seeded inventory and the deterministic identifier counters always start from the same state.

Prerequisites: JDK 21, Maven and Python 3 (standard library only — the script needs no third-party packages). The application must **not** already be running on the target port.

Text logs for one scenario:

```bash
python3 scripts/generate_springboot_logs.py --format text --scenario NORMAL
```

JSON logs for one scenario:

```bash
python3 scripts/generate_springboot_logs.py --format json --scenario NORMAL
```

Several scenarios at once, or every scenario:

```bash
python3 scripts/generate_springboot_logs.py --format json --scenario NORMAL OUT_OF_STOCK
python3 scripts/generate_springboot_logs.py --format json --scenario ALL
```

Both formats for every scenario:

```bash
python3 scripts/generate_springboot_logs.py --format all --scenario ALL
```

## CLI Options

| Option | Default | Description |
| ------ | ------- | ----------- |
| `--format` | `text` | `text`, `json`, or `all` for both. |
| `--scenario` | `NORMAL` | One or more scenarios, or `ALL` for every scenario. |
| `--output-dir` | `datasets/springboot-logback` | Directory the datasets are written to. |
| `--port` | `8080` | HTTP port the application is started on. |
| `--java` | `java` | Java executable to use. |
| `--startup-timeout` | `90` | Seconds to wait for the HTTP service to become ready. |
| `--request-timeout` | `60` | Seconds to wait for a benchmark run to finish. |
| `--rebuild` | off | Rebuild the jar before generating. |
| `--skip-build` | off | Fail instead of building a missing jar. |
| `--keep-raw` | off | Also write the complete process output next to each dataset. |

The script exits with a non-zero status if the build fails, the port is taken, the application does not become ready, the benchmark request fails, or the captured records do not match the requested scenario.

## Generated Files

Datasets are written to `datasets/springboot-logback/` and named `<SCENARIO>.<format>.log`:

```
datasets/springboot-logback/NORMAL.text.log
datasets/springboot-logback/NORMAL.json.log
datasets/springboot-logback/INVALID_ORDER.text.log
datasets/springboot-logback/INVALID_ORDER.json.log
datasets/springboot-logback/OUT_OF_STOCK.text.log
datasets/springboot-logback/OUT_OF_STOCK.json.log
datasets/springboot-logback/PAYMENT_DECLINED.text.log
datasets/springboot-logback/PAYMENT_DECLINED.json.log
datasets/springboot-logback/SHIPPING_DELAY.text.log
datasets/springboot-logback/SHIPPING_DELAY.json.log
```

A dataset holds only the benchmark business records — Spring Boot startup logs, the banner and any other process output are excluded. With `--keep-raw` the untouched process output is kept alongside as `<SCENARIO>.<format>.raw.log`. `.json.log` files are JSONL: every line is a complete JSON object.

---

# Supported Scenarios

| Scenario | Description |
| -------- | ----------- |
| `NORMAL` | The order is validated, stock is reserved, the payment is authorized, a shipment is created and the order completes. |
| `INVALID_ORDER` | The order is created with a quantity of zero, so validation rejects it and the workflow stops. |
| `OUT_OF_STOCK` | The order asks for 101 units of a product seeded with 100, so the reservation reports a shortage and the order is cancelled. |
| `PAYMENT_DECLINED` | Stock is reserved, the payment is declined, the reservation is released and the order is cancelled. |
| `SHIPPING_DELAY` | The order is paid and shipped, the shipment is delayed, and the order still completes. |

---

# Notes

* Seed data is deterministic: `PRODUCT-10` … `PRODUCT-19` with 100 units each, and `CUSTOMER-42` … `CUSTOMER-51`.
* Identifiers are deterministic: `REQ-1001`, `TRACE-1001`, `ORDER-5001`, `PAYMENT-7001`, `SHIPMENT-9001`.
* Only timestamps and the Tomcat worker thread name vary between runs; every other field is reproducible.
* The startup smoke workflow (`BenchmarkLoggingSmokeTest`) runs a `NORMAL` scenario on boot and stays enabled by default. Dataset generation starts the application with `--benchmark.smoke-test.enabled=false` so a generated file contains only the scenario requested over HTTP, against untouched seed data.
* `POST /api/benchmark/run` exists solely for benchmark dataset generation. Controllers emit no business log records of their own; every record comes from the business services.
