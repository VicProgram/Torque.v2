from sqlalchemy import Column, String, Text, ForeignKey
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship

from app.database import Base
from app.models.base import UUIDMixin, TimestampMixin


class Cliente(UUIDMixin, TimestampMixin, Base):
    __tablename__ = "clientes"

    id_taller = Column(
        UUID(as_uuid=True),
        ForeignKey("talleres.id"),
        nullable=False,
    )
    nombre = Column(String(200), nullable=False)
    telefono = Column(String(20), default="")
    email = Column(String(200), default="")
    direccion = Column(Text, default="")
    nif = Column(String(20), default="")
    notas = Column(Text, default="")

    taller = relationship("Taller", backref="clientes")
