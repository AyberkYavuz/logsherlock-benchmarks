import logging

from fastapi import APIRouter

from .models import PredictionRequest
from .services import InferenceService

logger = logging.getLogger("api")

router = APIRouter()


@router.get("/health")
def health():

    logger.info("Health check")

    return {"status": "healthy"}


@router.get("/models")
def models():

    logger.info("Fetching models")

    return InferenceService.available_models()


@router.post("/predict")
def predict(request: PredictionRequest):

    logger.info("Prediction request received")

    result = InferenceService.predict(request.text)

    logger.info("Prediction request completed")

    return result


@router.post("/reload-model")
def reload_model():

    logger.info("Reload model endpoint called")

    InferenceService.reload_model()

    return {"status": "ok"}
