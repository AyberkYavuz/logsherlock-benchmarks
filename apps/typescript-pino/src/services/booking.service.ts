import { Logger } from "pino";

import { Booking } from "../models/booking";
import { hotelService } from "./hotel.service";
import { paymentService } from "./payment.service";
import { notificationService } from "./notification.service";

class BookingService {
  private bookingCounter = 1;

  public createBooking(
    requestLogger: Logger,
    hotelId: string,
    customerId: string,
    nights: number,
  ): Booking {
    const bookingId = `BK-${String(this.bookingCounter++).padStart(6, "0")}`;

    const bookingLogger = requestLogger.child({
      service: "booking",
      bookingId,
      customerId,
      hotelId,
    });

    bookingLogger.info(
      {
        event: "booking_workflow_started",
        nights,
      },
      "Booking workflow started",
    );

    hotelService.checkAvailability(
      bookingLogger,
      hotelId,
    );

    bookingLogger.info(
      {
        event: "hotel_validated",
      },
      "Hotel availability validated",
    );

    paymentService.authorizePayment(
      bookingLogger,
      bookingId,
    );

    bookingLogger.info(
      {
        event: "payment_completed",
      },
      "Payment completed",
    );

    const booking: Booking = {
      bookingId,
      customerId,
      hotelId,
      nights,
      status: "confirmed",
    };

    bookingLogger.info(
      {
        event: "booking_created",
      },
      "Booking created",
    );

    notificationService.sendConfirmation(
      bookingLogger,
      bookingId,
      customerId,
    );

    bookingLogger.info(
      {
        event: "booking_workflow_completed",
      },
      "Booking workflow completed",
    );

    return booking;
  }
}

export const bookingService = new BookingService();
