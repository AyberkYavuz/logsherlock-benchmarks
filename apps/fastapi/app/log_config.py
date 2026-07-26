from copy import deepcopy
from uvicorn.config import LOGGING_CONFIG as UVICORN_LOGGING_CONFIG

LOGGING_CONFIG = deepcopy(UVICORN_LOGGING_CONFIG)

formatter = "%(asctime)s %(levelprefix)s %(message)s"
datefmt = "%Y-%m-%d %H:%M:%S"

LOGGING_CONFIG["formatters"]["default"]["fmt"] = formatter
LOGGING_CONFIG["formatters"]["default"]["datefmt"] = datefmt

LOGGING_CONFIG["formatters"]["access"]["fmt"] = formatter
LOGGING_CONFIG["formatters"]["access"]["datefmt"] = datefmt
