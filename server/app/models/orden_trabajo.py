from sqlalchemy import (
    DECIMAL,
    Column,
    Date,
    DateTime,
    ForeignKey,
    Integer,
    String,
    Text,
)
from sqlalchemy.orm import relationship

from app.database import Base
from app.models.base import GUID, TimestampMixin, UUIDMixin


class OrdenTrabajo(UUIDMixin, TimestampMixin, Base):
    __tablename__ = "ordenes_trabajo"

    id_taller = Column(
        GUID(),
        ForeignKey("talleres.id"),
        nullable=False,
    )
    id_vehiculo = Column(
        GUID(),
        ForeignKey("vehiculos.id"),
        nullable=False,
    )
    id_cliente = Column(
        GUID(),
        ForeignKey("clientes.id"),
        nullable=True,
    )
    id_mecanico_asignado = Column(
        GUID(),
        ForeignKey("usuarios.id"),
        nullable=True,
    )
    estado = Column(
        String(20),
        nullable=False,
        default="PRESUPUESTO",
    )
    fecha_entrada = Column(
        DateTime(timezone=True),
        nullable=True,
    )
    fecha_salida_estimada = Column(Date, nullable=True)
    fecha_completado = Column(DateTime(timezone=True), nullable=True)
    kilometros_entrada = Column(Integer, nullable=True)
    descripcion_problema = Column(Text, default="")
    diagnostico = Column(Text, default="")
    presupuesto_total = Column(DECIMAL(10, 2), nullable=True)
    total_real = Column(DECIMAL(10, 2), nullable=True)
    notas_internas = Column(Text, default="")
    notas_cliente = Column(Text, default="")

    taller = relationship("Taller", backref="ordenes")
    vehiculo = relationship("Vehiculo", backref="ordenes")
    cliente = relationship("Cliente", backref="ordenes")
    mecanico = relationship("Usuario", backref="ordenes_asignadas")
    detalles = relationship(
        "DetalleTrabajo",
        backref="orden_trabajo_rel",
        cascade="all, delete-orphan",
        order_by="DetalleTrabajo.orden",
    )
