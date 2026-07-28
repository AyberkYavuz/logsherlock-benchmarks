import express from "express";

import { healthRouter } from "./routes/health.routes.js";
import { requestIdMiddleware } from "./middleware/request-id.js";
import { requestLoggerMiddleware } from "./middleware/request-logger.js";

export const app = express();

app.use(express.json());

app.use(requestIdMiddleware);

app.use(requestLoggerMiddleware);

app.use(healthRouter);
