import random
import time
import uuid

import httpx

BASE_URL = "http://127.0.0.1:3000"

client = httpx.Client(timeout=10.0)

HOTELS = [
    "HTL-001",
    "HTL-002",
    "HTL-003",
    "HTL-004",
    "HTL-005",
]


def random_booking():
    return {
        "hotelId": random.choice(HOTELS),
        "customerId": f"CUS-{random.randint(1, 999999):06d}",
        "nights": random.randint(1, 7),
    }


def sleep():
    time.sleep(random.uniform(0.05, 0.20))


def create_booking(expected_status: int):
    response = client.post(
        f"{BASE_URL}/bookings",
        json=random_booking(),
    )

    assert response.status_code == expected_status, (
        response.status_code,
        response.text,
    )

    sleep()


print("======================================================")
print("Booking Benchmark Dataset Generation")
print("======================================================")

#
# Startup verification
#

print("\nHealth check...")

response = client.get(f"{BASE_URL}/health")
assert response.status_code == 200

sleep()

print("Loading hotel catalog...")

response = client.get(f"{BASE_URL}/hotels")
assert response.status_code == 200

sleep()

#
# Healthy traffic
#

print("\nGenerating healthy bookings...")

for _ in range(50):
    create_booking(201)

#
# Payment outage
#

print("\nSwitching to PAYMENT_PROVIDER_DOWN")

response = client.post(
    f"{BASE_URL}/scenario",
    json={
        "scenario": "payment_provider_down",
    },
)

assert response.status_code == 200

time.sleep(1)

print("Generating failed payment traffic...")

for _ in range(50):
    create_booking(503)

#
# Application still alive
#

print("\nHealth check during incident...")

response = client.get(f"{BASE_URL}/health")
assert response.status_code == 200

sleep()

print("Listing hotels during incident...")

response = client.get(f"{BASE_URL}/hotels")
assert response.status_code == 200

sleep()

#
# Recovery
#

print("\nRecovering application")

response = client.post(
    f"{BASE_URL}/scenario",
    json={
        "scenario": "normal",
    },
)

assert response.status_code == 200

time.sleep(1)

print("Generating recovered bookings...")

for _ in range(50):
    create_booking(201)

#
# Capacity incident
#

print("\nSwitching to NO_ROOMS_AVAILABLE")

response = client.post(
    f"{BASE_URL}/scenario",
    json={
        "scenario": "no_rooms_available",
    },
)

assert response.status_code == 200

time.sleep(1)

print("Generating no-room failures...")

for _ in range(40):
    create_booking(409)

#
# Recovery
#

print("\nReturning to NORMAL")

response = client.post(
    f"{BASE_URL}/scenario",
    json={
        "scenario": "normal",
    },
)

assert response.status_code == 200

time.sleep(1)

print("Generating final healthy traffic...")

for _ in range(60):
    create_booking(201)

#
# Final verification
#

print("\nFinal health check...")

response = client.get(f"{BASE_URL}/health")
assert response.status_code == 200

sleep()

print("Final hotel catalog request...")

response = client.get(f"{BASE_URL}/hotels")
assert response.status_code == 200

client.close()

print("\n======================================================")
print("Dataset generation completed successfully.")
print("======================================================")
