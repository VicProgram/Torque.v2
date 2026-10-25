from datetime import UTC, datetime

from sqlalchemy import Boolean, Column, DateTime, ForeignKey, String
from sqlalchemy.orm import relationship

from app.database import Base
from app.models.base import GUID, UUIDMixin


class Usuario(UUIDMixin, Base):
    __tablename__ = "usuarios"

    id_taller = Column(
        GUID(),
        ForeignKey("talleres.id"),
        nullable=False,
    )
    nombre = Column(String(100), nullable=False)
    email = Column(String(200), unique=True, nullable=False, index=True)
    password_hash = Column(String(200), nullable=False)
    pin_code = Column(String(6), nullable=True)
    rol = Column(
        String(20),
        nullable=False,
        default="MECANICO",
    )
    telefono = Column(String(20), default="")
    activo = Column(Boolean, default=True)
    creado_en = Column(
        DateTime(timezone=True),
        default=lambda: datetime.now(UTC),
        nullable=False,
    )
    ultimo_acceso = Column(DateTime(timezone=True), nullable=True)

    taller = relationship("Taller", backref="usuarios")
