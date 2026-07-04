from sqlalchemy import (
    Column, String, Integer, Date, Text, ForeignKey, UniqueConstraint,
)
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship

from app.database import Base
from app.models.base import UUIDMixin, TimestampMixin


class Vehiculo(UUIDMixin, TimestampMixin, Base):
    __tablename__ = "vehiculos"

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
    matricula = Column(String(20), default="")
    marca = Column(String(100), nullable=False)
    modelo = Column(String(100), nullable=False)
    anio = Column(Integer, default=0)
    bastidor = Column(String(50), default="")
    tipo_vehiculo = Column(String(20), default="MOTO")
    tipo_combustible = Column(String(20), default="")
    tipo_carroceria = Column(String(30), default="")
    transmision = Column(String(20), default="MANUAL")
    numero_puertas = Column(Integer, nullable=True)
    cilindrada = Column(String(10), default="")
    caballos = Column(String(10), default="")
    color = Column(String(30), default="")
    kilometros = Column(Integer, default=0)
    fecha_itv = Column(Date, nullable=True)
    fecha_seguro = Column(Date, nullable=True)
    notas_taller = Column(Text, default="")

    __table_args__ = (
        UniqueConstraint("id_taller", "matricula", name="uq_taller_matricula"),
    )

    taller = relationship("Taller", backref="vehiculos")
    cliente = relationship("Cliente", backref="vehiculos")
