import { NextFunction, Request, Response } from "express";

import { logger } from "../logger/logger.js";

export function requestLoggerMiddleware(
  req: Request,
  res: Response,
  next: NextFunction,
) {
  req.logger = logger.child({
    reqId: req.reqId,
  });

  req.logger.info(
    {
      event: "http_request_started",
      method: req.method,
      url: req.originalUrl,
    },
    "Incoming request",
  );

  res.on("finish", () => {
    req.logger.info(
      {
        event: "http_request_completed",
        method: req.method,
        url: req.originalUrl,
        statusCode: res.statusCode,
      },
      "Request completed",
    );
  });

  next();
}
