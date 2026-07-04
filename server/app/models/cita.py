from sqlalchemy import (
    Column, String, Text, Integer, ForeignKey, DateTime,
)
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship

from app.database import Base
from app.models.base import UUIDMixin, TimestampMixin


class Cita(UUIDMixin, TimestampMixin, Base):
    __tablename__ = "citas"

    id_taller = Column(
        UUID(as_uuid=True),
        ForeignKey("talleres.id"),
        nullable=False,
    )
    id_cliente = Column(
        UUID(as_uuid=True),
        ForeignKey("clientes.id"),
        nullable=True,
    )
    id_vehiculo = Column(
        UUID(as_uuid=True),
        ForeignKey("vehiculos.id"),
        nullable=True,
    )
    id_mecanico = Column(
        UUID(as_uuid=True),
        ForeignKey("usuarios.id"),
        nullable=True,
    )
    fecha_hora = Column(DateTime(timezone=True), nullable=False)
    duracion_estimada = Column(Integer, default=60)
    motivo = Column(Text, default="")
    estado = Column(
        String(20),
        nullable=False,
        default="PENDIENTE",
    )
    notas = Column(Text, default="")

    taller = relationship("Taller", backref="citas")
    cliente = relationship("Cliente")
    vehiculo = relationship("Vehiculo")
    mecanico = relationship("Usuario")
