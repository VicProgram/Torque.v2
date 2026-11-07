import uuid
from datetime import UTC, datetime

from sqlalchemy import Column, DateTime, String, Text

from app.database import Base
from app.models.base import GUID


class AuditLog(Base):
    """Append-only security and change audit trail.

    Rows deliberately store only identifiers and metadata. Request bodies,
    passwords, PINs and tokens must never be written here.
    """

    __tablename__ = "audit_logs"

    id = Column(
        GUID(),
        primary_key=True,
        default=uuid.uuid4,
        index=True,
    )
    creado_en = Column(
        DateTime(timezone=True),
        default=lambda: datetime.now(UTC),
        nullable=False,
        index=True,
    )
    id_taller = Column(GUID(), nullable=True, index=True)
    id_usuario = Column(GUID(), nullable=True, index=True)
    actor_email = Column(String(200), nullable=True)
    accion = Column(String(100), nullable=False, index=True)
    entidad = Column(String(80), nullable=True)
    entidad_id = Column(String(100), nullable=True)
    resultado = Column(String(20), nullable=False, default="SUCCESS")
    ip = Column(String(64), nullable=True)
    user_agent = Column(String(500), nullable=True)
    request_id = Column(String(64), nullable=True)
    detalle = Column(Text, nullable=True)
