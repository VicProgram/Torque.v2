import uuid
from datetime import date, datetime

from pydantic import BaseModel


class VehiculoBase(BaseModel):
    matricula: str = ""
    marca: str
    modelo: str
    anio: int = 0
    bastidor: str = ""
    tipo_vehiculo: str = "MOTO"
    tipo_combustible: str = ""
    tipo_carroceria: str = ""
    transmision: str = "MANUAL"
    numero_puertas: int | None = None
    cilindrada: str = ""
    caballos: str = ""
    color: str = ""
    kilometros: int = 0
    fecha_itv: date | None = None
    fecha_seguro: date | None = None
    notas_taller: str = ""


class VehiculoCreate(VehiculoBase):
    id_cliente: str | None = None


class VehiculoUpdate(BaseModel):
    matricula: str | None = None
    marca: str | None = None
    modelo: str | None = None
    anio: int | None = None
    bastidor: str | None = None
    tipo_vehiculo: str | None = None
    tipo_combustible: str | None = None
    tipo_carroceria: str | None = None
    transmision: str | None = None
    numero_puertas: int | None = None
    cilindrada: str | None = None
    caballos: str | None = None
    color: str | None = None
    kilometros: int | None = None
    fecha_itv: date | None = None
    fecha_seguro: date | None = None
    notas_taller: str | None = None
    id_cliente: str | None = None


class VehiculoResponse(VehiculoBase):
    id: uuid.UUID
    id_taller: uuid.UUID
    id_cliente: uuid.UUID | None = None
    creado_en: datetime
    actualizado_en: datetime

    model_config = {"from_attributes": True}


class BuscarMatriculaResponse(BaseModel):
    id: uuid.UUID
    matricula: str
    marca: str
    modelo: str
    cliente_nombre: str = ""
    cliente_id: uuid.UUID | None = None
