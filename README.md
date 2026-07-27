# LogSherlock Benchmarks

## About LogSherlock

[LogSherlock](https://github.com/AyberkYavuz/log-sherlock) is an open-source AI-powered log investigation system built with LangGraph. It aims to help developers analyze application logs using deterministic processing, AI agents, and structured reasoning to identify incidents, understand failures, and accelerate root cause analysis. The project is being developed incrementally with a strong focus on production-quality architecture, extensibility, and comprehensive testing.

## About LogSherlock Benchmarks

LogSherlock Benchmarks is the companion repository for LogSherlock. It contains small applications and dataset generation scripts used to produce realistic benchmark log files for developing and validating LogSherlock. Instead of relying on synthetic or manually written logs, this repository generates deterministic datasets that simulate real production scenarios such as healthy operation, service failures, timeouts, and recovery events.

## FastAPI AI Simulation

The repository currently includes a FastAPI-based AI inference simulation application. It can generate multiple benchmark datasets by running predefined traffic generation scripts under different runtime scenarios.

For setup instructions, available scenarios, and dataset generation commands, see:

* `docs/fastapi.md`

## Roadmap

Additional benchmark applications are planned, including:

* TypeScript + Pino
* Java Spring Boot

These applications will expand the benchmark corpus with different logging ecosystems and production-style failure scenarios, providing broader coverage for LogSherlock development and testing.
