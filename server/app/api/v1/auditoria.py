from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.api.deps import require_rol
from app.database import get_db
from app.models.audit_log import AuditLog
from app.models.usuario import Usuario
from app.schemas.auditoria import AuditLogResponse

router = APIRouter()


@router.get("/eventos", response_model=list[AuditLogResponse])
def listar_eventos(
    accion: str | None = None,
    resultado: str | None = None,
    limite: int = Query(100, ge=1, le=500),
    desplazamiento: int = Query(0, ge=0),
    user: Usuario = Depends(require_rol("ADMIN")),
    db: Session = Depends(get_db),
):
    query = db.query(AuditLog).filter(AuditLog.id_taller == user.id_taller)
    if accion:
        query = query.filter(AuditLog.accion == accion)
    if resultado:
        query = query.filter(AuditLog.resultado == resultado)
    return (
        query.order_by(AuditLog.creado_en.desc())
        .offset(desplazamiento)
        .limit(limite)
        .all()
    )
