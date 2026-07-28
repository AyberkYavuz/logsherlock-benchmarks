import { Router } from "express";

import { CreateBookingRequest } from "../models/create-booking-request";
import { bookingService } from "../services/booking.service";

export const bookingRouter = Router();

bookingRouter.post("/", (req, res) => {
  const body = req.body as CreateBookingRequest;

  try {
    const booking = bookingService.createBooking(
      req.logger,
      body.hotelId,
      body.customerId,
      body.nights,
    );

    return res.status(201).json(booking);
  } catch (error) {
    const message =
      error instanceof Error ? error.message : "Unknown error";

    req.logger.error(
      {
        event: "booking_failed",
        error: message,
      },
      "Booking request failed",
    );

    req.logger.error(
      {
        event: "booking_workflow_failed",
      },
      "Booking workflow failed",
    );

    if (message === "No rooms available") {
      return res.status(409).json({
        error: message,
      });
    }

    if (message === "Payment provider unavailable") {
      return res.status(503).json({
        error: message,
      });
    }

    if (message === "Payment timeout") {
      return res.status(504).json({
        error: message,
      });
    }

    if (message.startsWith("Unknown hotel")) {
      return res.status(404).json({
        error: message,
      });
    }

    return res.status(500).json({
      error: message,
    });
  }
});
