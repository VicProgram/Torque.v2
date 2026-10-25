from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import JSONResponse


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


def register_exception_handlers(app: FastAPI):
    @app.exception_handler(AppException)
    async def app_exception_handler(request: Request, exc: AppException):
        return JSONResponse(
            status_code=exc.status_code,
            content={"detail": exc.message},
        )

    @app.exception_handler(HTTPException)
    async def http_exception_handler(request: Request, exc: HTTPException):
        return JSONResponse(
            status_code=exc.status_code,
            content={"detail": exc.detail},
        )
