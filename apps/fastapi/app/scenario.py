from enum import Enum


class Scenario(Enum):
    NORMAL = "normal"
    MODEL_NOT_LOADED = "model_not_loaded"
    INFERENCE_TIMEOUT = "inference_timeout"


class ScenarioManager:

    def __init__(self):
        self.current = Scenario.NORMAL

    def set(self, scenario: Scenario):
        self.current = scenario

    def get(self):
        return self.current


scenario_manager = ScenarioManager()
