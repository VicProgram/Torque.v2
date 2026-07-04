from sqlalchemy import Column, String, Text, Boolean, DECIMAL

from app.database import Base
from app.models.base import UUIDMixin, TimestampMixin


class Taller(UUIDMixin, TimestampMixin, Base):
    __tablename__ = "talleres"

    nombre = Column(String(200), nullable=False)
    direccion = Column(Text, default="")
    telefono = Column(String(20), default="")
    email = Column(String(200), default="")
    web = Column(String(200), default="")
    nif = Column(String(20), default="")
    logo_url = Column(Text, default="")
    tipo_iva = Column(DECIMAL(5, 2), default=21.00)
    divisa = Column(String(3), default="EUR")
    pie_pagina_factura = Column(Text, default="")
    activo = Column(Boolean, default=True)
