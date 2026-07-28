import { logger } from "../logger/logger";

const notificationLogger = logger.child({
  service: "notification",
});

class NotificationService {
  public sendConfirmation(
    bookingId: string,
    customerId: string,
  ): void {
    notificationLogger.info(
      {
        event: "confirmation_sent",
        bookingId,
        customerId,
      },
      "Booking confirmation sent",
    );
  }
}

export const notificationService = new NotificationService();
