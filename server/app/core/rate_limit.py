import threading
import time
from collections import deque

from fastapi import HTTPException, Request, status

from app.config import get_settings

settings = get_settings()


class SlidingWindowLimiter:
    """Small in-process limiter for single-worker deployments.

    Multi-worker or multi-instance deployments must use the configured Redis
    instance (or an API gateway) for a shared counter. The application still
    benefits from this baseline when running as a single Uvicorn worker.
    """

    def __init__(self) -> None:
        self._events: dict[str, deque[float]] = {}
        self._lock = threading.Lock()

    def hit(self, key: str, limit: int, window_seconds: int) -> int | None:
        now = time.monotonic()
        cutoff = now - window_seconds
        with self._lock:
            bucket = self._events.setdefault(key, deque())
            while bucket and bucket[0] <= cutoff:
                bucket.popleft()
            if len(bucket) >= limit:
                retry_after = max(1, int(bucket[0] + window_seconds - now) + 1)
                return retry_after
            bucket.append(now)
            # Avoid unbounded growth from one-off clients.
            if len(self._events) > 10_000:
                self._prune(cutoff)
            return None

    def _prune(self, cutoff: float) -> None:
        empty = [key for key, bucket in self._events.items() if not bucket or bucket[-1] <= cutoff]
        for key in empty:
            self._events.pop(key, None)

    def reset(self) -> None:
        with self._lock:
            self._events.clear()


limiter = SlidingWindowLimiter()


def rate_limit(request: Request, bucket: str, limit: int, window_seconds: int) -> None:
    client = request.client.host if request.client else "unknown"
    retry_after = limiter.hit(f"{bucket}:{client}", limit, window_seconds)
    if retry_after is not None:
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail="Demasiados intentos. Inténtalo de nuevo más tarde.",
            headers={"Retry-After": str(retry_after)},
        )


def rate_limit_login(request: Request) -> None:
    rate_limit(
        request,
        "auth.login",
        settings.login_rate_limit_attempts,
        settings.login_rate_limit_window_seconds,
    )


def rate_limit_pin(request: Request) -> None:
    rate_limit(
        request,
        "auth.pin",
        settings.login_rate_limit_attempts,
        settings.login_rate_limit_window_seconds,
    )


def rate_limit_registration(request: Request) -> None:
    rate_limit(
        request,
        "auth.register",
        max(3, settings.login_rate_limit_attempts // 2),
        3600,
    )
