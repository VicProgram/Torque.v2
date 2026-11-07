import logging

from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import JSONResponse
from sqlalchemy.exc import IntegrityError

logger = logging.getLogger("torque.errors")


class AppException(Exception):
    def __init__(self, message: str, status_code: int = 400):
        self.message = message
        self.status_code = status_code


class NotFoundException(AppException):
    def __init__(self, entity: str = "Recurso"):
        super().__init__(f"{entity} no encontrado", status_code=404)


class UnauthorizedException(AppException):
    def __init__(self, message: str = "No autorizado"):
        super().__init__(message, status_code=401)


class ForbiddenException(AppException):
    def __init__(self, message: str = "Acceso denegado"):
        super().__init__(message, status_code=403)


class ConflictException(AppException):
    def __init__(self, message: str):
        super().__init__(message, status_code=409)


def _request_id(request: Request) -> str | None:
    return getattr(request.state, "request_id", None)


def register_exception_handlers(app: FastAPI):
    @app.exception_handler(AppException)
    async def app_exception_handler(request: Request, exc: AppException):
        headers = {"X-Request-ID": _request_id(request)} if _request_id(request) else None
        return JSONResponse(
            status_code=exc.status_code,
            content={"detail": exc.message, "request_id": _request_id(request)},
            headers=headers,
        )

    @app.exception_handler(HTTPException)
    async def http_exception_handler(request: Request, exc: HTTPException):
        headers = dict(exc.headers or {})
        if _request_id(request):
            headers["X-Request-ID"] = _request_id(request)
        return JSONResponse(
            status_code=exc.status_code,
            content={"detail": exc.detail, "request_id": _request_id(request)},
            headers=headers,
        )

    @app.exception_handler(IntegrityError)
    async def integrity_exception_handler(request: Request, exc: IntegrityError):
        logger.warning(
            "integrity_error",
            extra={"request_id": _request_id(request), "path": request.url.path},
        )
        headers = {"X-Request-ID": _request_id(request)} if _request_id(request) else None
        return JSONResponse(
            status_code=409,
            content={
                "detail": "La operación entra en conflicto con datos existentes",
                "request_id": _request_id(request),
            },
            headers=headers,
        )

    @app.exception_handler(Exception)
    async def unhandled_exception_handler(request: Request, exc: Exception):
        # Never return stack traces or internal details to clients. The full
        # exception, path and request ID go to the structured security log.
        logger.exception(
            "unhandled_exception",
            extra={
                "request_id": _request_id(request),
                "method": request.method,
                "path": request.url.path,
            },
        )
        headers = {"X-Request-ID": _request_id(request)} if _request_id(request) else None
        return JSONResponse(
            status_code=500,
            content={
                "detail": "Error interno del servidor",
                "request_id": _request_id(request),
            },
            headers=headers,
        )
