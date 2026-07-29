# TypeScript Pino Booking Application

## Overview

The TypeScript Pino application simulates a hotel booking service for the LogSherlock benchmark suite. It generates realistic structured JSON logs using Pino that can be used to develop, test, and validate LogSherlock's log parsing, statistics, and future analysis capabilities.

The application supports multiple runtime scenarios (normal operation, payment provider failures, room availability failures, and recovery) to produce deterministic benchmark datasets.

---

# Running the Application

Install dependencies:

```bash
cd apps/typescript-pino
npm install
```

Build the application:

```bash
npm run build
```

Start the application:

```bash
npm run dev
```

The application starts on:

```
http://localhost:3000
```

---

# Manual Verification

After starting the application, verify that it behaves correctly by executing the following requests.

## Health Check

```bash
curl http://localhost:3000/health
```

Expected response:

```json
{
  "status": "healthy"
}
```

---

## List Available Hotels

```bash
curl http://localhost:3000/hotels
```

Returns the hotel catalog used by the benchmark application.

---

## Create a Successful Booking

```bash
curl -X POST http://localhost:3000/bookings \
-H "Content-Type: application/json" \
-d '{
  "hotelId":"HTL-001",
  "customerId":"CUS-000001",
  "nights":3
}'
```

Expected response:

```json
{
  "bookingId":"BK-000001",
  "customerId":"CUS-000001",
  "hotelId":"HTL-001",
  "nights":3,
  "status":"confirmed"
}
```

---

## Switch Runtime Scenario

The `/scenario` endpoint exists only for benchmark dataset generation.

Switch to a payment failure scenario:

```bash
curl -X POST http://localhost:3000/scenario \
-H "Content-Type: application/json" \
-d '{
  "scenario":"payment_provider_down"
}'
```

Expected response:

```json
{
  "status":"success",
  "scenario":"payment_provider_down"
}
```

---

## Verify Failure Scenario

After changing the runtime scenario, submit another booking request.

```bash
curl -X POST http://localhost:3000/bookings \
-H "Content-Type: application/json" \
-d '{
  "hotelId":"HTL-001",
  "customerId":"CUS-000001",
  "nights":3
}'
```

Expected response:

```json
{
  "error":"Payment provider unavailable"
}
```

This confirms that runtime scenario switching is working correctly.

---

# Dataset Generation

After starting the TypeScript application, execute the dataset generation script.

```bash
python3 scripts/generate_booking_dataset.py
```

The script automatically generates realistic traffic including:

- Health checks
- Hotel catalog requests
- Successful bookings
- Payment failures
- Recovery
- No-room failures
- Final healthy traffic

The generated logs are intended for LogSherlock development and regression testing.

---

# Supported Runtime Scenarios

| Scenario | Description |
|-----------|-------------|
| `normal` | All booking requests succeed. |
| `payment_provider_down` | Payment authorization fails for every booking request. |
| `payment_timeout` | Payment authorization times out. |
| `no_rooms_available` | Hotel availability checks fail because no rooms are available. |

---

# Notes

* The `/scenario` endpoint exists solely for benchmark dataset generation.
* Benchmark scripts generate deterministic logs intended for LogSherlock development and regression testing.
* The application is designed to simulate realistic production booking workflows rather than implement a complete hotel reservation system.
* All application logs are emitted as structured JSON using Pino to facilitate log analysis.
