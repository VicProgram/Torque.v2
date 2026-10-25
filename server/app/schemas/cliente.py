import uuid
from datetime import datetime

from pydantic import BaseModel


class ClienteBase(BaseModel):
    nombre: str
    telefono: str = ""
    email: str = ""
    direccion: str = ""
    nif: str = ""
    notas: str = ""


class ClienteCreate(ClienteBase):
    pass


class ClienteUpdate(BaseModel):
    nombre: str | None = None
    telefono: str | None = None
    email: str | None = None
    direccion: str | None = None
    nif: str | None = None
    notas: str | None = None


class ClienteResponse(ClienteBase):
    id: uuid.UUID
    id_taller: uuid.UUID
    creado_en: datetime
    actualizado_en: datetime

    model_config = {"from_attributes": True}
