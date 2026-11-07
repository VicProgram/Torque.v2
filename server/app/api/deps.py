import logging

from fastapi import Depends, HTTPException, Request, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy.orm import Session

from app.core.audit import actor_from_request, audit_event, set_audit_actor
from app.core.exceptions import NotFoundException
from app.core.security import decode_access_token
from app.database import get_db
from app.models.usuario import Usuario

logger = logging.getLogger("torque.auth")
security_scheme = HTTPBearer(auto_error=False)

_UNAUTHORIZED_HEADERS = {"WWW-Authenticate": "Bearer"}


def get_current_user(
    request: Request,
    credentials: HTTPAuthorizationCredentials | None = Depends(security_scheme),
    db: Session = Depends(get_db),
) -> Usuario:
    if credentials is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token requerido",
            headers=_UNAUTHORIZED_HEADERS,
        )
    payload = decode_access_token(credentials.credentials)
    if payload is None:
        logger.warning(
            "invalid_token",
            extra={"request_id": getattr(request.state, "request_id", None)},
        )
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token inválido o expirado",
            headers=_UNAUTHORIZED_HEADERS,
        )
    user_id = payload.get("sub")
    if user_id is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token inválido",
            headers=_UNAUTHORIZED_HEADERS,
        )
    user = db.query(Usuario).filter(Usuario.id == user_id).first()
    if user is None or not user.activo:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Credenciales no válidas",
            headers=_UNAUTHORIZED_HEADERS,
        )
    if int(payload.get("ver", 0)) != int(user.token_version or 0):
        logger.warning(
            "revoked_token",
            extra={
                "request_id": getattr(request.state, "request_id", None),
                "actor_id": str(user.id),
            },
        )
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Sesión revocada. Vuelve a iniciar sesión.",
            headers=_UNAUTHORIZED_HEADERS,
        )

    actor = actor_from_request(request, user=user)
    set_audit_actor(actor)
    # FastAPI can run a sync dependency and the sync endpoint in different
    # worker threads, so the shared SQLAlchemy session is the reliable place
    # to keep the actor for the automatic ORM audit listener.
    db.info["audit_actor"] = actor
    return user


def require_taller_record(db: Session, model, record_id: str | None, taller_id, entity: str):
    """Return a record owned by the workshop, or 404 if it belongs to another tenant."""
    if record_id is None:
        return None
    record = db.query(model).filter(
        model.id == record_id,
        model.id_taller == taller_id,
    ).first()
    if record is None:
        raise NotFoundException(entity)
    return record


def require_rol(*roles: str):
    def role_checker(
        request: Request,
        user: Usuario = Depends(get_current_user),
        db: Session = Depends(get_db),
    ) -> Usuario:
        if user.rol not in roles:
            audit_event(
                db,
                "authz.denied",
                outcome="DENIED",
                entity="endpoint",
                entity_id=request.url.path,
                detail=f"rol_requerido={','.join(roles)} rol_actual={user.rol}",
            )
            db.commit()
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="No tienes permisos para esta operación",
            )
        return user
    return role_checker
