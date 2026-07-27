import random
import time

import httpx

BASE_URL = "http://127.0.0.1:8000"

client = httpx.Client(timeout=10.0)

print("=== Starting recovery benchmark dataset generation ===")

# ---------------------------------------------------------
# Startup verification
# ---------------------------------------------------------

print("Health check...")
response = client.get(f"{BASE_URL}/health")
assert response.status_code == 200

time.sleep(0.5)

print("Fetching available models...")
response = client.get(f"{BASE_URL}/models")
assert response.status_code == 200

time.sleep(0.5)

# ---------------------------------------------------------
# Healthy traffic
# ---------------------------------------------------------

print("Generating healthy traffic...")

for i in range(10):
    response = client.post(
        f"{BASE_URL}/predict",
        json={
            "model": "sentiment-v1",
            "text": f"Healthy request #{i}",
        },
    )

    assert response.status_code == 200

    time.sleep(random.uniform(0.15, 0.35))

# ---------------------------------------------------------
# Incident starts
# ---------------------------------------------------------

print("Switching scenario to MODEL_NOT_LOADED")

response = client.post(
    f"{BASE_URL}/scenario",
    json={
        "scenario": "model_not_loaded"
    },
)

assert response.status_code == 200

time.sleep(1)

# ---------------------------------------------------------
# Failed traffic
# ---------------------------------------------------------

print("Generating failed traffic...")

for i in range(10):
    response = client.post(
        f"{BASE_URL}/predict",
        json={
            "model": "sentiment-v1",
            "text": f"Incident request #{i}",
        },
    )

    assert response.status_code == 503

    time.sleep(random.uniform(0.15, 0.35))

# ---------------------------------------------------------
# Service still alive
# ---------------------------------------------------------

print("Health check during incident...")

response = client.get(f"{BASE_URL}/health")
assert response.status_code == 200

time.sleep(0.5)

# ---------------------------------------------------------
# Recovery
# ---------------------------------------------------------

print("Switching scenario back to NORMAL")

response = client.post(
    f"{BASE_URL}/scenario",
    json={
        "scenario": "normal"
    },
)

assert response.status_code == 200

time.sleep(1)

# ---------------------------------------------------------
# Healthy traffic again
# ---------------------------------------------------------

print("Generating recovered traffic...")

for i in range(10):
    response = client.post(
        f"{BASE_URL}/predict",
        json={
            "model": "sentiment-v1",
            "text": f"Recovered request #{i}",
        },
    )

    assert response.status_code == 200

    time.sleep(random.uniform(0.15, 0.35))

# ---------------------------------------------------------
# Administrative action
# ---------------------------------------------------------

print("Reloading model...")

response = client.post(
    f"{BASE_URL}/reload-model",
    json={
        "model": "sentiment-v1"
    },
)

assert response.status_code == 200

time.sleep(1)

# ---------------------------------------------------------
# Final verification
# ---------------------------------------------------------

print("Checking models...")
response = client.get(f"{BASE_URL}/models")
assert response.status_code == 200

print("Final health check...")
response = client.get(f"{BASE_URL}/health")
assert response.status_code == 200

client.close()

print("=== Dataset generation completed ===")
