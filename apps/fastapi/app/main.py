import logging
import os
from fastapi import FastAPI

from .api import router

from .scenario import Scenario, scenario_manager

logger = logging.getLogger("uvicorn.error")

app = FastAPI()

app.include_router(router)

scenario = os.getenv("SCENARIO", "normal")

scenario_manager.set(Scenario(scenario))


@app.on_event("startup")
async def startup():
    logger.info("Loading AI models...")
    logger.info("Loaded model sentiment-v1")
    logger.info("Loaded model embeddings-v2")
    logger.info("Initializing inference engine...")
    logger.info("Inference service ready")


@app.on_event("shutdown")
async def shutdown():
    logger.info("Application shutdown")
