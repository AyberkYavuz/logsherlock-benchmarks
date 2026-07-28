import { app } from "./app";
import { logger } from "./logger/logger";

const PORT = 3000;

logger.info(
  {
    event: "startup_started",
  },
  "Application startup initiated",
);

logger.info(
  {
    event: "loading_hotel_catalog",
  },
  "Loading hotel catalog",
);

logger.info(
  {
    event: "hotel_catalog_loaded",
    hotelCount: 5,
  },
  "Hotel catalog loaded",
);

logger.info(
  {
    event: "initializing_payment_provider",
  },
  "Initializing payment provider",
);

logger.info(
  {
    event: "payment_provider_ready",
  },
  "Payment provider ready",
);

logger.info(
  {
    event: "booking_service_initialized",
  },
  "Booking service initialized",
);

app.listen(PORT, () => {
  logger.info(
    {
      event: "application_started",
      port: PORT,
    },
    "Booking benchmark application started",
  );
});
