import logging
import random
import time

logger = logging.getLogger("ai-service")


class InferenceService:

    MODEL_NAME = "sentiment-v1"

    @classmethod
    def predict(cls, text: str):

        logger.info("Starting inference using model=%s", cls.MODEL_NAME)

        start = time.perf_counter()

        time.sleep(random.uniform(0.05, 0.2))

        latency = int((time.perf_counter() - start) * 1000)

        logger.info("Inference completed latency_ms=%s", latency)

        return {
            "label": random.choice(["positive", "negative", "neutral"]),
            "confidence": round(random.uniform(0.90, 0.99), 2),
        }

    @classmethod
    def available_models(cls):

        logger.info("Listing available models")

        return [
            cls.MODEL_NAME,
            "emotion-v2",
            "topic-v1",
        ]

    @classmethod
    def reload_model(cls):

        logger.warning("Reloading model=%s", cls.MODEL_NAME)

        time.sleep(1)

        logger.info("Model reloaded successfully")
