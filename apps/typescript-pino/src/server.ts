import { app } from "./app.js";
import { logger } from "./logger/logger.js";

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
