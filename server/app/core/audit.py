import logging
from contextvars import ContextVar
from dataclasses import dataclass

from sqlalchemy import event, inspect
from sqlalchemy.orm import Session

from app.models.audit_log import AuditLog

logger = logging.getLogger("torque.audit")


@dataclass(frozen=True)
class AuditActor:
    usuario_id: str | None = None
    taller_id: str | None = None
    email: str | None = None
    ip: str | None = None
    user_agent: str | None = None
    request_id: str | None = None
    method: str | None = None
    path: str | None = None


current_actor: ContextVar[AuditActor | None] = ContextVar(
    "torque_audit_actor", default=None
)


def set_audit_actor(actor: AuditActor):
    return current_actor.set(actor)


def reset_audit_actor(token) -> None:
    current_actor.reset(token)


def get_audit_actor() -> AuditActor | None:
    return current_actor.get()


def client_ip(request) -> str | None:
    if request is None or request.client is None:
        return None
    return request.client.host


def actor_from_request(request, user=None, email: str | None = None) -> AuditActor:
    request_id = None
    method = None
    path = None
    user_agent = None
    ip = None
    if request is not None:
        request_id = getattr(request.state, "request_id", None)
        method = request.method
        path = request.url.path
        user_agent = (request.headers.get("user-agent") or "")[:500] or None
        ip = client_ip(request)
    return AuditActor(
        usuario_id=str(user.id) if user is not None else None,
        taller_id=str(user.id_taller) if user is not None else None,
        email=email or (getattr(user, "email", None) if user is not None else None),
        ip=ip,
        user_agent=user_agent,
        request_id=request_id,
        method=method,
        path=path,
    )


def audit_event(
    db: Session,
    accion: str,
    *,
    actor: AuditActor | None = None,
    outcome: str = "SUCCESS",
    entity: str | None = None,
    entity_id: str | None = None,
    taller_id=None,
    usuario_id=None,
    email: str | None = None,
    detail: str | None = None,
) -> None:
    """Persist one explicit security event.

    Use this for authentication, authorization and other events that are not
    ordinary ORM writes. Normal data changes are captured automatically by the
    ``before_flush`` listener below.
    """
    actor = actor or db.info.get("audit_actor") or get_audit_actor()
    row = AuditLog(
        id_taller=taller_id or (actor.taller_id if actor else None),
        id_usuario=usuario_id or (actor.usuario_id if actor else None),
        actor_email=email or (actor.email if actor else None),
        accion=accion,
        entidad=entity,
        entidad_id=str(entity_id) if entity_id is not None else None,
        resultado=outcome,
        ip=actor.ip if actor else None,
        user_agent=actor.user_agent if actor else None,
        request_id=actor.request_id if actor else None,
        detalle=detail,
    )
    try:
        # A savepoint keeps a failed audit write from poisoning the caller's
        # transaction while still committing with the business change.
        with db.begin_nested():
            db.add(row)
    except Exception:  # pragma: no cover - defensive; logging must never break requests
        logger.exception("audit_write_failed", extra={"accion": accion})
        return

    logger.info(
        "audit_event",
        extra={
            "event": "audit",
            "accion": accion,
            "resultado": outcome,
            "entidad": entity,
            "entidad_id": str(entity_id) if entity_id is not None else None,
            "actor_id": row.id_usuario,
            "taller_id": row.id_taller,
            "request_id": row.request_id,
        },
    )


def _changed_fields(obj) -> str | None:
    try:
        state = inspect(obj)
        fields = sorted(
            attribute.key
            for attribute in state.attrs
            if attribute.history.has_changes()
        )
    except Exception:  # pragma: no cover - best-effort metadata
        return None
    return ",".join(fields)[:1000] or None


@event.listens_for(AuditLog, "before_update")
def _reject_audit_update(mapper, connection, target) -> None:
    raise ValueError("audit_logs is append-only; updates are not allowed")


@event.listens_for(AuditLog, "before_delete")
def _reject_audit_delete(mapper, connection, target) -> None:
    raise ValueError("audit_logs is append-only; deletes are not allowed")


@event.listens_for(Session, "before_flush")
def _audit_orm_changes(session, flush_context, instances) -> None:
    """Create an audit row for every ORM insert, update or delete.

    Only runs when an authenticated request set the actor context, so seeds,
    migrations and background jobs do not create noisy rows.
    """
    actor = session.info.get("audit_actor") or get_audit_actor()
    if actor is None:
        return

    changes: list[tuple[str, object, str | None]] = []
    for obj in session.new:
        if isinstance(obj, AuditLog):
            continue
        changes.append(("CREATE", obj, None))
    for obj in session.dirty:
        if isinstance(obj, AuditLog):
            continue
        if session.is_modified(obj, include_collections=False):
            changes.append(("UPDATE", obj, _changed_fields(obj)))
    for obj in session.deleted:
        if isinstance(obj, AuditLog):
            continue
        changes.append(("DELETE", obj, None))

    for action, obj, fields in changes:
        session.add(
            AuditLog(
                id_taller=getattr(obj, "id_taller", None) or actor.taller_id,
                id_usuario=actor.usuario_id,
                actor_email=actor.email,
                accion=f"data.{action.lower()}",
                entidad=getattr(obj, "__tablename__", None),
                entidad_id=str(getattr(obj, "id", "") or "") or None,
                resultado="SUCCESS",
                ip=actor.ip,
                user_agent=actor.user_agent,
                request_id=actor.request_id,
                detalle=fields,
            )
        )
