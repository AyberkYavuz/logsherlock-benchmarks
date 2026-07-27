import random
import time

import httpx

BASE_URL = "http://127.0.0.1:8000"

client = httpx.Client(timeout=10.0)

print("=== Starting model_not_loaded benchmark dataset generation ===")

# ---------------------------------------------------------
# Initial startup checks
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
# Failed prediction burst #1
# ---------------------------------------------------------

print("Generating failed prediction burst #1")

for i in range(15):
    response = client.post(
        f"{BASE_URL}/predict",
        json={
            "model": "sentiment-v1",
            "text": f"Failed benchmark request #{i}",
        },
    )

    assert response.status_code == 503

    time.sleep(random.uniform(0.15, 0.35))

# ---------------------------------------------------------
# Health verification
# ---------------------------------------------------------

print("Health check...")
response = client.get(f"{BASE_URL}/health")
assert response.status_code == 200

time.sleep(0.5)

# ---------------------------------------------------------
# Administrative operation
# ---------------------------------------------------------

print("Attempting model reload...")

response = client.post(
    f"{BASE_URL}/reload-model",
    json={
        "model": "sentiment-v1",
    },
)

assert response.status_code == 200

time.sleep(1)

# ---------------------------------------------------------
# Failed prediction burst #2
# ---------------------------------------------------------

print("Generating failed prediction burst #2")

for i in range(15, 20):
    response = client.post(
        f"{BASE_URL}/predict",
        json={
            "model": "sentiment-v1",
            "text": f"Failed benchmark request #{i}",
        },
    )

    assert response.status_code == 503

    time.sleep(random.uniform(0.15, 0.35))

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
