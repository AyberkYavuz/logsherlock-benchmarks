import random
import time

import httpx

BASE_URL = "http://127.0.0.1:8000"

client = httpx.Client(timeout=10.0)

print("=== Starting normal benchmark dataset generation ===")

# ---------------------------------------------------------
# Initial startup checks
# ---------------------------------------------------------

print("Health check...")
client.get(f"{BASE_URL}/health")

time.sleep(0.5)

print("Fetching available models...")
client.get(f"{BASE_URL}/models")

time.sleep(0.5)

# ---------------------------------------------------------
# First prediction burst
# ---------------------------------------------------------

print("Generating prediction burst #1")

for i in range(30):
    client.post(
        f"{BASE_URL}/predict",
        json={
            "model": "sentiment-v1",
            "text": f"Benchmark request #{i}",
        },
    )

    time.sleep(random.uniform(0.15, 0.35))

# ---------------------------------------------------------
# Health check
# ---------------------------------------------------------

print("Health check...")
client.get(f"{BASE_URL}/health")

time.sleep(0.5)

# ---------------------------------------------------------
# Second prediction burst
# ---------------------------------------------------------

print("Generating prediction burst #2")

for i in range(30, 50):
    client.post(
        f"{BASE_URL}/predict",
        json={
            "model": "sentiment-v1",
            "text": f"Benchmark request #{i}",
        },
    )

    time.sleep(random.uniform(0.15, 0.35))

# ---------------------------------------------------------
# Administrative operation
# ---------------------------------------------------------

print("Reloading model...")

client.post(
    f"{BASE_URL}/reload-model",
    json={
        "model": "sentiment-v1",
    },
)

time.sleep(1)

# ---------------------------------------------------------
# Service verification
# ---------------------------------------------------------

print("Checking models...")
client.get(f"{BASE_URL}/models")

print("Health check...")
client.get(f"{BASE_URL}/health")

time.sleep(0.5)

# ---------------------------------------------------------
# Final prediction burst
# ---------------------------------------------------------

print("Generating prediction burst #3")

for i in range(50, 65):
    client.post(
        f"{BASE_URL}/predict",
        json={
            "model": "sentiment-v1",
            "text": f"Benchmark request #{i}",
        },
    )

    time.sleep(random.uniform(0.15, 0.35))

# ---------------------------------------------------------
# Final health check
# ---------------------------------------------------------

print("Final health check...")
client.get(f"{BASE_URL}/health")

print("=== Dataset generation completed ===")
