# Spring Boot + Logback Benchmark

Production-style Spring Boot benchmark application for LogSherlock Benchmarks.

Status:

- [x] Project bootstrap
- [x] Logging
- [x] Scenario engine
- [x] Workflow
- [x] HTTP API
- [x] Dataset generation

## Log Formats

The application emits the same business log events either as plaintext or as JSON.
The format is selected at startup through `LOG_FORMAT`, so no source change and no
rebuild is required:

```bash
# plaintext (default)
java -jar target/springboot-logback-0.0.1-SNAPSHOT.jar

# JSON, one object per line
LOG_FORMAT=json java -jar target/springboot-logback-0.0.1-SNAPSHOT.jar
```

## Dataset Generation

```bash
python3 scripts/generate_springboot_logs.py --format text --scenario NORMAL
python3 scripts/generate_springboot_logs.py --format json --scenario NORMAL
```

Generated datasets are written to `datasets/springboot-logback/`.

Full documentation, including the supported scenarios, the log record fields and
every generator option:

* `docs/springboot-logback.md`
