import { Router } from "express";

export const healthRouter = Router();

healthRouter.get("/health", (req, res) => {
  req.logger.info(
    {
      event: "health_check",
    },
    "Health check requested",
  );

  res.json({
    status: "healthy",
  });
});
