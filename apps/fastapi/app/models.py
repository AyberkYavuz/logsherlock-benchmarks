from pydantic import BaseModel


class PredictionRequest(BaseModel):
    model: str
    text: str


class PredictionResponse(BaseModel):
    label: str
    confidence: float
    latency_ms: int


class ModelInfo(BaseModel):
    name: str
    status: str


class ReloadModelRequest(BaseModel):
    model: str


class ScenarioRequest(BaseModel):
    scenario: str

