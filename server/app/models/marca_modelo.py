import uuid

from sqlalchemy import Column, ForeignKey, String, UniqueConstraint
from sqlalchemy.dialects.sqlite import CHAR
from sqlalchemy.orm import relationship

from app.models.base import Base


class MarcaVehiculo(Base):
    __tablename__ = "marcas_vehiculo"

    id = Column(CHAR(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    nombre = Column(String(100), nullable=False)
    tipo_vehiculo = Column(String(10), nullable=False)

    modelos = relationship(
        "ModeloVehiculo", back_populates="marca",
        cascade="all, delete-orphan",
        order_by="ModeloVehiculo.nombre",
    )

    __table_args__ = (
        UniqueConstraint("nombre", "tipo_vehiculo",
                         name="uq_marca_tipo"),
    )


class ModeloVehiculo(Base):
    __tablename__ = "modelos_vehiculo"

    id = Column(CHAR(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    id_marca = Column(CHAR(36), ForeignKey("marcas_vehiculo.id"), nullable=False)
    nombre = Column(String(100), nullable=False)

    marca = relationship("MarcaVehiculo", back_populates="modelos")
