from sqlalchemy import DECIMAL, Column, ForeignKey, Integer, String
from sqlalchemy.orm import relationship

from app.database import Base
from app.models.base import GUID, TimestampMixin, UUIDMixin


class Articulo(UUIDMixin, TimestampMixin, Base):
    __tablename__ = "articulos"

    id_taller = Column(
        GUID(),
        ForeignKey("talleres.id"),
        nullable=False,
    )
    nombre = Column(String(200), nullable=False)
    sku = Column(String(50), default="")
    categoria = Column(String(100), default="")
    cantidad_stock = Column(Integer, default=0)
    stock_minimo = Column(Integer, default=0)
    precio_venta = Column(DECIMAL(10, 2), nullable=True)
    precio_coste = Column(DECIMAL(10, 2), nullable=True)
    proveedor = Column(String(200), default="")

    taller = relationship("Taller", backref="articulos")
