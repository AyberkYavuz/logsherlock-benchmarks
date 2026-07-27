import logging
import time

from .models import ModelInfo, PredictionResponse
from .scenario import Scenario, scenario_manager

logger = logging.getLogger("uvicorn.error")


class InferenceService:
    """Simple AI inference simulation."""

    def __init__(self):
        self._models = {
            "sentiment-v1": "loaded",
            "embeddings-v2": "loaded",
        }

    def available_models(self) -> list[ModelInfo]:
        logger.info("Fetching available models")

        models = [
            ModelInfo(name=name, status=status)
            for name, status in self._models.items()
        ]

        logger.info("Returning %d available models", len(models))

        return models

    def predict(self, model: str, text: str) -> PredictionResponse:
        logger.info("Prediction request received model=%s text_length=%d", model, len(text))
        logger.info("Validating model '%s'", model)

        if model not in self._models:
            logger.error("Unknown model '%s'", model)
            raise ValueError(f"Unknown model: {model}")

        logger.info("Starting inference using model '%s'", model)

        start = time.perf_counter()

        scenario = scenario_manager.get()

        if scenario == Scenario.MODEL_NOT_LOADED:
            raise RuntimeError(f"Model not loaded: {model}")

        if scenario == Scenario.INFERENCE_TIMEOUT:
            time.sleep(5)
            raise TimeoutError(f"Inference timeout exceeded for model: {model}")

        # Simulate inference
        time.sleep(0.12)

        latency_ms = int((time.perf_counter() - start) * 1000)

        logger.info(
            "Inference completed model=%s latency_ms=%d",
            model,
            latency_ms,
        )

        logger.info("Prediction request completed")

        return PredictionResponse(
            label="positive",
            confidence=0.98,
            latency_ms=latency_ms,
        )

    def reload_model(self, model: str):
        if model not in self._models:
            logger.error("Unknown model '%s'", model)
            raise ValueError(f"Unknown model: {model}")

        logger.warning(f"Reloading model '{model}'")

        time.sleep(0.5)

        logger.info(f"Model '{model}' reloaded successfully")

    def set_scenario(self, scenario: Scenario):
        previous = scenario_manager.get()

        if previous == scenario:
            logger.info("Scenario already '%s'", scenario.value)
            return

        logger.warning(
            "Scenario changed from %s to %s",
            previous.value,
            scenario.value,
        )

        scenario_manager.set(scenario)


inference_service = InferenceService()
