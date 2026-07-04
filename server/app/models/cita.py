from sqlalchemy import (
    Column, String, Text, Integer, ForeignKey, DateTime,
)
from sqlalchemy.orm import relationship

from app.database import Base
from app.models.base import UUIDMixin, TimestampMixin, GUID


class Cita(UUIDMixin, TimestampMixin, Base):
    __tablename__ = "citas"

    id_taller = Column(
        GUID(),
        ForeignKey("talleres.id"),
        nullable=False,
    )
    id_cliente = Column(
        GUID(),
        ForeignKey("clientes.id"),
        nullable=True,
    )
    id_vehiculo = Column(
        GUID(),
        ForeignKey("vehiculos.id"),
        nullable=True,
    )
    id_mecanico = Column(
        GUID(),
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
