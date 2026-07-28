import pino from "pino";

export const logger = pino({
  level: "info",

  timestamp: pino.stdTimeFunctions.isoTime,

  base: {
    application: "booking-benchmark",
  },

  formatters: {
    level(label) {
      return {
        level: label,
      };
    },
  },
});
