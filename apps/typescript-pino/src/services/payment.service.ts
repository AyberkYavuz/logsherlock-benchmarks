import { logger } from "../logger/logger";
import { scenarioManager } from "../scenario/scenario-manager";
import { Scenario } from "../scenario/scenario";

const paymentLogger = logger.child({
  service: "payment",
});

class PaymentService {
  public authorizePayment(bookingId: string): void {
    paymentLogger.info(
      {
        event: "payment_started",
        bookingId,
      },
      "Authorizing payment",
    );

    const scenario = scenarioManager.get();

    if (scenario === Scenario.PAYMENT_PROVIDER_DOWN) {
      paymentLogger.error(
        {
          event: "payment_provider_down",
          bookingId,
        },
        "Payment provider unavailable",
      );

      throw new Error("Payment provider unavailable");
    }

    if (scenario === Scenario.PAYMENT_TIMEOUT) {
      paymentLogger.error(
        {
          event: "payment_timeout",
          bookingId,
        },
        "Payment request timed out",
      );

      throw new Error("Payment timeout");
    }

    paymentLogger.info(
      {
        event: "payment_authorized",
        bookingId,
      },
      "Payment authorized",
    );
  }
}

export const paymentService = new PaymentService();
