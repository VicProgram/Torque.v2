import uuid
from datetime import datetime, timezone

from sqlalchemy import Column, String, Integer, Text, ForeignKey, DateTime
from sqlalchemy.orm import relationship

from app.database import Base
from app.models.base import GUID


class Archivo(Base):
    __tablename__ = "archivos"

    id = Column(
        GUID(),
        primary_key=True,
        default=uuid.uuid4,
        index=True,
    )
    id_taller = Column(
        GUID(),
        ForeignKey("talleres.id"),
        nullable=False,
    )
    entidad_tipo = Column(String(50), nullable=False)
    entidad_id = Column(GUID(), nullable=False)
    nombre_archivo = Column(String(200), default="")
    tipo_mime = Column(String(100), default="")
    ruta = Column(Text, nullable=False)
    tamano_bytes = Column(Integer, nullable=True)
    creado_en = Column(
        DateTime(timezone=True),
        default=lambda: datetime.now(timezone.utc),
        nullable=False,
    )

    taller = relationship("Taller", backref="archivos")
