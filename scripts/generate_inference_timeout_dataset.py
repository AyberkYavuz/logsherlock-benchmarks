import random
import time

import httpx

BASE_URL = "http://127.0.0.1:8000"

client = httpx.Client(timeout=10.0)

print("=== Starting inference_timeout benchmark dataset generation ===")

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
# Timeout burst #1
# ---------------------------------------------------------

print("Generating timeout burst #1")

for i in range(10):
    response = client.post(
        f"{BASE_URL}/predict",
        json={
            "model": "sentiment-v1",
            "text": f"Timeout benchmark request #{i}",
        },
    )

    assert response.status_code == 504

    time.sleep(random.uniform(0.15, 0.35))

# ---------------------------------------------------------
# Health verification
# ---------------------------------------------------------

print("Health check...")
response = client.get(f"{BASE_URL}/health")
assert response.status_code == 200

time.sleep(0.5)

# ---------------------------------------------------------
# Timeout burst #2
# ---------------------------------------------------------

print("Generating timeout burst #2")

for i in range(10, 20):
    response = client.post(
        f"{BASE_URL}/predict",
        json={
            "model": "sentiment-v1",
            "text": f"Timeout benchmark request #{i}",
        },
    )

    assert response.status_code == 504

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
