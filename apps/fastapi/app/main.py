import logging
import logging.config

from fastapi import FastAPI

from .api import router


LOGGING_CONFIG = {
    "version": 1,
    "disable_existing_loggers": False,
    "formatters": {
        "default": {
            "format": (
                "%(asctime)s | "
                "%(levelname)-8s | "
                "%(name)-15s | "
                "%(message)s"
            ),
            "datefmt": "%Y-%m-%d %H:%M:%S",
        },
    },
    "handlers": {
        "console": {
            "formatter": "default",
            "class": "logging.StreamHandler",
        },
    },
    "root": {
        "handlers": ["console"],
        "level": "INFO",
    },
}


logging.config.dictConfig(LOGGING_CONFIG)

logger = logging.getLogger("app")

app = FastAPI(title="LogSherlock Benchmark AI Service")

app.include_router(router)


@app.on_event("startup")
def startup():

    logger.info("Application startup")


@app.on_event("shutdown")
def shutdown():

    logger.info("Application shutdown")
