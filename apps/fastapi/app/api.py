import logging

from fastapi import APIRouter, HTTPException

from .models import PredictionRequest, ReloadModelRequest
from .services import inference_service

logger = logging.getLogger("uvicorn.error")

router = APIRouter()


@router.get("/health")
def health():
    logger.info("Health check requested")

    return {"status": "healthy"}


@router.get("/models")
def models():
    return inference_service.available_models()


@router.post("/predict")
def predict(request: PredictionRequest):
    try:
        return inference_service.predict(
            model=request.model,
            text=request.text,
        )

    except ValueError as exc:
        logger.error(str(exc))
        raise HTTPException(status_code=404, detail=str(exc))


@router.post("/reload-model")
def reload_model(request: ReloadModelRequest):
    try:
        inference_service.reload_model(request.model)

        return {
            "status": "success",
            "message": f"Model '{request.model}' reloaded successfully.",
        }

    except ValueError as exc:
        logger.error(str(exc))
        raise HTTPException(status_code=404, detail=str(exc))
