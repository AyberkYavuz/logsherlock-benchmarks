from enum import Enum


class Scenario(Enum):
    NORMAL = "normal"
    MODEL_NOT_LOADED = "model_not_loaded"
    INFERENCE_TIMEOUT = "inference_timeout"
