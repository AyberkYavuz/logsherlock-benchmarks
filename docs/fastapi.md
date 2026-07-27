# FastAPI AI Simulation Application

## Overview

The FastAPI application simulates a simple AI inference service for the LogSherlock benchmark suite. It generates realistic application logs that can be used to develop, test, and validate LogSherlock's log parsing, statistics, and future analysis capabilities.

The application supports multiple runtime scenarios (normal operation, model failures, inference timeouts, and recovery) to produce deterministic benchmark datasets.

---

# Running the Application

Install dependencies:

```bash
pip3 install -r apps/fastapi/requirements.txt
```

Start the application in one of the supported scenarios.

Normal:

```bash
SCENARIO=normal python3 apps/fastapi/run.py
```

Model Not Loaded:

```bash
SCENARIO=model_not_loaded python3 apps/fastapi/run.py
```

Inference Timeout:

```bash
SCENARIO=inference_timeout python3 apps/fastapi/run.py
```

---

# Dataset Generation

After starting the FastAPI application, execute one of the dataset generation scripts.

## Normal Dataset

Starts the application in normal mode and generates healthy traffic.

```bash
python3 scripts/generate_normal_dataset.py
```

Produces:

```
datasets/fastapi/normal.log
```

---

## Model Not Loaded Dataset

Starts the application with the `model_not_loaded` scenario and generates repeated prediction failures.

```bash
python3 scripts/generate_model_not_loaded_dataset.py
```

Produces:

```
datasets/fastapi/model_not_loaded.log
```

---

## Inference Timeout Dataset

Starts the application with the `inference_timeout` scenario and generates timeout errors.

```bash
python3 scripts/generate_inference_timeout_dataset.py
```

Produces:

```
datasets/fastapi/inference_timeout.log
```

---

## Recovery Dataset

Starts the application in normal mode and dynamically changes the runtime scenario using the benchmark-only `/scenario` endpoint.

The generated log demonstrates:

* Normal operation
* Runtime incident
* Service recovery

```bash
python3 scripts/generate_recovery_dataset.py
```

Produces:

```
datasets/fastapi/recovery.log
```

---

# Supported Runtime Scenarios

| Scenario            | Description                                                |
| ------------------- | ---------------------------------------------------------- |
| `normal`            | All prediction requests succeed.                           |
| `model_not_loaded`  | Prediction requests fail because the model is unavailable. |
| `inference_timeout` | Prediction requests fail due to inference timeout.         |

---

# Notes

* The `/scenario` endpoint exists solely for benchmark dataset generation.
* Benchmark scripts generate deterministic logs intended for LogSherlock development and regression testing.
* The application is designed to simulate realistic production log patterns rather than perform actual machine learning inference.
