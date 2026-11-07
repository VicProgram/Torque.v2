import uuid
from datetime import datetime

from pydantic import BaseModel


class AuditLogResponse(BaseModel):
    id: uuid.UUID
    creado_en: datetime
    id_taller: uuid.UUID | None = None
    id_usuario: uuid.UUID | None = None
    actor_email: str | None = None
    accion: str
    entidad: str | None = None
    entidad_id: str | None = None
    resultado: str
    ip: str | None = None
    request_id: str | None = None
    detalle: str | None = None

    model_config = {"from_attributes": True}
