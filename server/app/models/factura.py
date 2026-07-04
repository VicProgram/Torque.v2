from sqlalchemy import (
    Column, String, Text, Date, DECIMAL, ForeignKey, UniqueConstraint,
)
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship

from app.database import Base
from app.models.base import UUIDMixin


class Factura(UUIDMixin, Base):
    __tablename__ = "facturas"

    id_taller = Column(
        UUID(as_uuid=True),
        ForeignKey("talleres.id"),
        nullable=False,
    )
    id_orden_trabajo = Column(
        UUID(as_uuid=True),
        ForeignKey("ordenes_trabajo.id"),
        nullable=True,
    )
    numero_factura = Column(String(50), nullable=False)
    fecha_emision = Column(Date, nullable=False)
    fecha_vencimiento = Column(Date, nullable=True)
    base_imponible = Column(DECIMAL(10, 2), nullable=False)
    iva = Column(DECIMAL(5, 2), nullable=False, default=21.00)
    total = Column(DECIMAL(10, 2), nullable=False)
    estado = Column(
        String(20),
        nullable=False,
        default="EMITIDA",
    )
    metodo_pago = Column(String(30), default="")
    notas = Column(Text, default="")
    pdf_url = Column(Text, default="")

    __table_args__ = (
        UniqueConstraint(
            "id_taller", "numero_factura", name="uq_taller_numero_factura"
        ),
    )

    taller = relationship("Taller", backref="facturas")
    orden_trabajo = relationship("OrdenTrabajo", backref="facturas")
