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

    res.status(201).json(booking);
  } catch (error) {
    req.logger.error(
      {
        event: "booking_failed",
        error: error instanceof Error ? error.message : String(error),
      },
      "Booking request failed",
    );

    res.status(503).json({
      error: error instanceof Error ? error.message : "Unknown error",
    });
  }
});
