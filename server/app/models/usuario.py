from datetime import UTC, datetime

from sqlalchemy import Boolean, Column, DateTime, ForeignKey, Integer, String
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
    # Stores a bcrypt hash. The column keeps its historical name so existing
    # databases can be migrated without a destructive rename.
    pin_code = Column(String(200), nullable=True)
    # Secret-keyed HMAC of the PIN, used only for an indexed lookup.
    pin_lookup = Column(String(64), unique=True, nullable=True, index=True)
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
    # Incrementing this value revokes every token previously issued to the user.
    token_version = Column(Integer, nullable=False, default=0)
    failed_login_attempts = Column(Integer, nullable=False, default=0)
    locked_until = Column(DateTime(timezone=True), nullable=True)
    password_changed_at = Column(DateTime(timezone=True), nullable=True)

    taller = relationship("Taller", backref="usuarios")
