import express from "express";

import { requestIdMiddleware } from "./middleware/request-id";
import { requestLoggerMiddleware } from "./middleware/request-logger";

import { healthRouter } from "./routes/health.routes";
import { hotelRouter } from "./routes/hotel.routes";
import { bookingRouter } from "./routes/booking.routes";
import { scenarioRouter } from "./routes/scenario.routes";

export const app = express();

app.use(express.json());

app.use(requestIdMiddleware);
app.use(requestLoggerMiddleware);

app.use("/health", healthRouter);
app.use("/hotels", hotelRouter);
app.use("/bookings", bookingRouter);
app.use("/scenario", scenarioRouter);
