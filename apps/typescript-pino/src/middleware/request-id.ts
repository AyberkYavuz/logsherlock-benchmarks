import { NextFunction, Request, Response } from "express";
import { v4 as uuidv4 } from "uuid";

export function requestIdMiddleware(
  req: Request,
  _: Response,
  next: NextFunction,
) {
  req.reqId = uuidv4();

  next();
}
