import { Logger } from "pino";


class NotificationService {
  public sendConfirmation(
    requestLogger: Logger,
    bookingId: string,
    customerId: string,
  ): void {
    const notificationLogger = requestLogger.child({component:"notification"});
    notificationLogger.info(
      {
        event: "confirmation_sent"
      },
      "Booking confirmation sent",
    );
  }
}

export const notificationService = new NotificationService();
