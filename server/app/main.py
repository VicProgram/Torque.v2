import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.httpsredirect import HTTPSRedirectMiddleware
from fastapi.middleware.trustedhost import TrustedHostMiddleware

from app.api.router import api_router
from app.config import get_settings
from app.core.exceptions import register_exception_handlers
from app.core.logging import configure_logging
from app.core.middleware import RequestContextMiddleware, SecurityHeadersMiddleware
from app.core.websocket import websocket_router

settings = get_settings()
configure_logging(settings.log_level)
logger = logging.getLogger("torque.startup")


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info(
        "application_startup",
        extra={
            "event": "startup",
            "environment": settings.environment,
            "docs_enabled": settings.enable_docs,
            "force_https": settings.force_https,
        },
    )
    yield
    logger.info("application_shutdown", extra={"event": "shutdown"})


app = FastAPI(
    title="Torque Workshop API",
    version="0.1.0",
    description="Backend for multi-device workshop management",
    lifespan=lifespan,
    docs_url="/docs" if settings.enable_docs else None,
    redoc_url="/redoc" if settings.enable_docs else None,
    openapi_url="/openapi.json" if settings.enable_docs else None,
)

# Middleware is applied in reverse order of registration: request context and
# security headers are outermost so they also cover errors raised further in.
if settings.trusted_hosts_list and "*" not in settings.trusted_hosts_list:
    app.add_middleware(
        TrustedHostMiddleware,
        allowed_hosts=settings.trusted_hosts_list,
    )

if settings.force_https:
    app.add_middleware(HTTPSRedirectMiddleware)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.origins_list,
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"],
    allow_headers=["Authorization", "Content-Type", "X-Request-ID"],
    expose_headers=["X-Request-ID"],
    max_age=600,
)
app.add_middleware(SecurityHeadersMiddleware)
app.add_middleware(RequestContextMiddleware)

app.include_router(api_router, prefix="/api/v1")
app.include_router(websocket_router)

register_exception_handlers(app)


@app.get("/healthz", tags=["Salud"])
def healthz():
    return {"status": "ok"}
