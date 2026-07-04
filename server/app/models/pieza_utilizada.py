from sqlalchemy import Column, Integer, DECIMAL, ForeignKey
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship

from app.database import Base
from app.models.base import UUIDMixin


class PiezaUtilizada(UUIDMixin, Base):
    __tablename__ = "piezas_utilizadas"

    id_detalle_trabajo = Column(
        UUID(as_uuid=True),
        ForeignKey("detalles_trabajo.id"),
        nullable=False,
    )
    id_articulo = Column(
        UUID(as_uuid=True),
        ForeignKey("articulos.id"),
        nullable=False,
    )
    cantidad = Column(Integer, nullable=False, default=1)
    precio_unitario = Column(DECIMAL(10, 2), nullable=False)

    articulo = relationship("Articulo")
