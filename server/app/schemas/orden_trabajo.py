import uuid
from datetime import date, datetime
from decimal import Decimal
from pydantic import BaseModel


class DetalleTrabajoBase(BaseModel):
    id_tipo_mantenimiento: int | None = None
    descripcion: str
    precio_estimado: Decimal | None = None
    precio_real: Decimal | None = None
    tiempo_estimado: int | None = None
    tiempo_real: int | None = None
    notas_mecanico: str = ""
    orden: int = 0


class DetalleTrabajoCreate(DetalleTrabajoBase):
    pass


class DetalleTrabajoUpdate(BaseModel):
    descripcion: str | None = None
    estado: str | None = None
    precio_estimado: Decimal | None = None
    precio_real: Decimal | None = None
    tiempo_estimado: int | None = None
    tiempo_real: int | None = None
    notas_mecanico: str | None = None
    orden: int | None = None


class DetalleTrabajoResponse(DetalleTrabajoBase):
    id: uuid.UUID
    id_orden_trabajo: uuid.UUID
    estado: str

    model_config = {"from_attributes": True}


class OrdenTrabajoBase(BaseModel):
    id_vehiculo: str
    id_cliente: str | None = None
    id_mecanico_asignado: str | None = None
    descripcion_problema: str = ""
    kilometros_entrada: int | None = None
    fecha_salida_estimada: date | None = None
    notas_internas: str = ""
    notas_cliente: str = ""


class OrdenTrabajoCreate(OrdenTrabajoBase):
    pass


class OrdenTrabajoUpdate(BaseModel):
    id_vehiculo: str | None = None
    id_cliente: str | None = None
    id_mecanico_asignado: str | None = None
    descripcion_problema: str | None = None
    diagnostico: str | None = None
    kilometros_entrada: int | None = None
    presupuesto_total: Decimal | None = None
    total_real: Decimal | None = None
    fecha_salida_estimada: date | None = None
    notas_internas: str | None = None
    notas_cliente: str | None = None


class CambioEstadoRequest(BaseModel):
    estado: str


class OrdenTrabajoResponse(OrdenTrabajoBase):
    id: uuid.UUID
    id_taller: uuid.UUID
    id_vehiculo: uuid.UUID
    id_cliente: uuid.UUID | None = None
    id_mecanico_asignado: uuid.UUID | None = None
    estado: str
    diagnostico: str = ""
    presupuesto_total: Decimal | None = None
    total_real: Decimal | None = None
    fecha_entrada: datetime | None = None
    fecha_completado: datetime | None = None
    creado_en: datetime
    actualizado_en: datetime
    detalles: list[DetalleTrabajoResponse] = []

    model_config = {"from_attributes": True}
