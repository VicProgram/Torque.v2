import uuid
from datetime import date, datetime
from decimal import Decimal
from pydantic import BaseModel


class FacturaBase(BaseModel):
    id_orden_trabajo: str | None = None
    numero_factura: str
    fecha_emision: date
    fecha_vencimiento: date | None = None
    base_imponible: Decimal
    iva: Decimal = Decimal("21.00")
    total: Decimal
    metodo_pago: str = ""
    notas: str = ""


class FacturaCreate(FacturaBase):
    pass


class FacturaUpdate(BaseModel):
    estado: str | None = None
    metodo_pago: str | None = None
    notas: str | None = None


class FacturaResponse(FacturaBase):
    id: uuid.UUID
    id_taller: uuid.UUID
    id_orden_trabajo: uuid.UUID | None = None
    estado: str
    pdf_url: str = ""
    creado_en: datetime

    model_config = {"from_attributes": True}
