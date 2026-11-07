import json
import logging
import sys
from datetime import UTC, datetime

_LOG_EXTRA_FIELDS = (
    "event",
    "accion",
    "resultado",
    "entidad",
    "entidad_id",
    "actor_id",
    "taller_id",
    "request_id",
    "method",
    "path",
    "status_code",
    "duration_ms",
    "client",
)


class JsonFormatter(logging.Formatter):
    """Structured JSON logs suitable for shipping to a SIEM."""

    def format(self, record: logging.LogRecord) -> str:
        payload: dict[str, object] = {
            "ts": datetime.now(UTC).isoformat(),
            "level": record.levelname,
            "logger": record.name,
            "message": record.getMessage(),
        }
        for field in _LOG_EXTRA_FIELDS:
            value = getattr(record, field, None)
            if value is not None:
                payload[field] = value
        if record.exc_info:
            payload["exception"] = self.formatException(record.exc_info)
        return json.dumps(payload, ensure_ascii=False, default=str)


def configure_logging(level: str = "INFO") -> None:
    handler = logging.StreamHandler(sys.stdout)
    handler.setFormatter(JsonFormatter())
    root = logging.getLogger()
    root.handlers[:] = [handler]
    root.setLevel(level.upper())
    for noisy in ("uvicorn.access",):
        logging.getLogger(noisy).propagate = False
