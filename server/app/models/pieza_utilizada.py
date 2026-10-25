from sqlalchemy import DECIMAL, Column, ForeignKey, Integer
from sqlalchemy.orm import relationship

from app.database import Base
from app.models.base import GUID, UUIDMixin


class PiezaUtilizada(UUIDMixin, Base):
    __tablename__ = "piezas_utilizadas"

    id_detalle_trabajo = Column(
        GUID(),
        ForeignKey("detalles_trabajo.id"),
        nullable=False,
    )
    id_articulo = Column(
        GUID(),
        ForeignKey("articulos.id"),
        nullable=False,
    )
    cantidad = Column(Integer, nullable=False, default=1)
    precio_unitario = Column(DECIMAL(10, 2), nullable=False)

    articulo = relationship("Articulo")
