from sqlalchemy import (
    Column, String, Text, Integer, DECIMAL, ForeignKey,
)
from sqlalchemy.orm import relationship

from app.database import Base
from app.models.base import UUIDMixin, GUID


class DetalleTrabajo(UUIDMixin, Base):
    __tablename__ = "detalles_trabajo"

    id_orden_trabajo = Column(
        GUID(),
        ForeignKey("ordenes_trabajo.id"),
        nullable=False,
    )
    id_tipo_mantenimiento = Column(Integer, nullable=True)
    descripcion = Column(Text, nullable=False)
    estado = Column(
        String(20),
        nullable=False,
        default="PENDIENTE",
    )
    precio_estimado = Column(DECIMAL(10, 2), nullable=True)
    precio_real = Column(DECIMAL(10, 2), nullable=True)
    tiempo_estimado = Column(Integer, nullable=True)
    tiempo_real = Column(Integer, nullable=True)
    notas_mecanico = Column(Text, default="")
    orden = Column(Integer, default=0)

    piezas = relationship(
        "PiezaUtilizada",
        backref="detalle",
        cascade="all, delete-orphan",
    )
