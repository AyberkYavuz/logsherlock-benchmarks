import { NextFunction, Request, Response } from "express";

import { logger } from "../logger/logger";

export function requestLoggerMiddleware(
  req: Request,
  res: Response,
  next: NextFunction,
) {
  req.logger = logger.child({
    reqId: req.reqId,
  });

  const start = Date.now();

  req.logger.info(
    {
      event: "http_request_started",
      method: req.method,
      url: req.originalUrl,
    },
    "Incoming request",
  );

  res.on("finish", () => {
    const durationMs = Date.now() - start;
    req.logger.info(
      {
        event: "http_request_completed",
        method: req.method,
        url: req.originalUrl,
        statusCode: res.statusCode,
        durationMs
      },
      "Request completed",
    );
  });

  next();
}
