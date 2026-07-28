import { Logger } from "pino";

declare global {
  namespace Express {
    interface Request {
      reqId: string;
      logger: Logger;
    }
  }
}

export {};
