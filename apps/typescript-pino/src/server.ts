import { app } from "./app";
import { logger } from "./logger/logger";

const PORT = 3000;

app.listen(PORT, () => {
  logger.info(
    {
      event: "application_started",
      port: PORT,
    },
    "Booking benchmark application started"
  );
});
