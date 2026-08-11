# LogSherlock Benchmarks

## About LogSherlock

[LogSherlock](https://github.com/AyberkYavuz/log-sherlock) is an open-source AI-powered log investigation system built with LangGraph. It helps developers analyze application logs using deterministic processing, AI agents, and structured reasoning to identify incidents, understand failures, and accelerate root cause analysis. The project is being developed incrementally with a strong focus on production-quality architecture, extensibility, and comprehensive testing.

## About LogSherlock Benchmarks

LogSherlock Benchmarks is the companion repository for LogSherlock. It contains small applications and dataset generation scripts used to produce realistic benchmark log files for developing and validating LogSherlock. Instead of relying on synthetic or manually written logs, this repository generates deterministic datasets that simulate real production scenarios such as healthy operation, service failures, timeouts, and recovery events.

The goal of this repository is to provide benchmark datasets from multiple application stacks and logging ecosystems, enabling LogSherlock to be evaluated against realistic production-style logs.

## Benchmark Applications

### FastAPI AI Simulation

A Python FastAPI application that simulates an AI inference service. It generates structured application logs for scenarios such as:

* Normal operation
* Model not loaded
* Inference timeout
* Recovery

Documentation:

* `docs/fastapi.md`

---

### TypeScript Pino Booking Application

A TypeScript Express application using Pino that simulates a hotel booking service. It generates realistic structured JSON logs for scenarios such as:

* Normal booking workflow
* Payment provider failures
* Payment timeouts
* No rooms available
* Recovery

Documentation:

* `docs/typescript-pino.md`

## Repository Structure

```text
apps/
├── fastapi/
└── typescript-pino/

datasets/
├── fastapi/
└── typescript-pino/

docs/
├── fastapi.md
└── typescript-pino.md

scripts/
```

## Roadmap

Additional benchmark applications are planned to expand LogSherlock's coverage across different languages, frameworks, and logging ecosystems.

Planned benchmark applications include:

* Java Spring Boot
