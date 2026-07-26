import logging

from fastapi import FastAPI

from .api import router

logger = logging.getLogger("uvicorn.error")

app = FastAPI()

app.include_router(router)


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
