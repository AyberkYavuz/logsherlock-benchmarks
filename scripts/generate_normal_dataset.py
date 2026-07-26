import time

import httpx

BASE_URL = "http://127.0.0.1:8000"

client = httpx.Client(timeout=10.0)

print("Health check...")
client.get(f"{BASE_URL}/health")

time.sleep(0.3)

print("Fetching models...")
client.get(f"{BASE_URL}/models")

time.sleep(0.3)

texts = [
    "I love LogSherlock.",
    "FastAPI is awesome.",
    "The prediction looks good.",
    "Machine learning is fun.",
    "LangGraph is powerful.",
    "Today's weather is amazing.",
    "The deployment completed successfully.",
    "Everything looks healthy.",
    "Generate another prediction.",
    "AI engineering is exciting.",
]

print("Generating predictions...")

for i in range(5):
    client.post(
        f"{BASE_URL}/predict",
        json={
            "model": "sentiment-v1",
            "text": f"Benchmark request #{i}",
        }
    )
    time.sleep(0.2)

print("Reloading model...")

client.post(
    f"{BASE_URL}/reload-model",
    json={
        "model": "sentiment-v1"
    },
)

time.sleep(0.5)

print("Checking models again...")
client.get(f"{BASE_URL}/models")

time.sleep(0.3)

print("Final health check...")
client.get(f"{BASE_URL}/health")

print("Done.")
