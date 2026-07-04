from datetime import datetime
from pydantic import BaseModel


class CitaBase(BaseModel):
    id_cliente: str | None = None
    id_vehiculo: str | None = None
    id_mecanico: str | None = None
    fecha_hora: datetime
    duracion_estimada: int = 60
    motivo: str = ""
    notas: str = ""


class CitaCreate(CitaBase):
    pass


class CitaUpdate(BaseModel):
    id_cliente: str | None = None
    id_vehiculo: str | None = None
    id_mecanico: str | None = None
    fecha_hora: datetime | None = None
    duracion_estimada: int | None = None
    motivo: str | None = None
    estado: str | None = None
    notas: str | None = None


class CitaResponse(CitaBase):
    id: str
    id_taller: str
    estado: str
    creado_en: datetime
    actualizado_en: datetime

    model_config = {"from_attributes": True}
