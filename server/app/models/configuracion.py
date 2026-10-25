from sqlalchemy import DECIMAL, Column, ForeignKey, String, Text
from sqlalchemy.orm import relationship

from app.database import Base
from app.models.base import GUID, UUIDMixin


class ConfiguracionTaller(UUIDMixin, Base):
    __tablename__ = "configuracion_taller"

    id_taller = Column(
        GUID(),
        ForeignKey("talleres.id"),
        unique=True,
        nullable=False,
    )
    nombre_taller = Column(String(200), default="")
    direccion = Column(Text, default="")
    telefono = Column(String(20), default="")
    email = Column(String(200), default="")
    web = Column(String(200), default="")
    tipo_iva = Column(DECIMAL(5, 2), default=21.00)
    divisa = Column(String(3), default="EUR")
    pie_pagina_factura = Column(Text, default="")
    logo_url = Column(Text, default="")

    taller = relationship("Taller", backref="configuracion")
