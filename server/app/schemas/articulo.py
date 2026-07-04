from datetime import datetime
from decimal import Decimal
from pydantic import BaseModel


class ArticuloBase(BaseModel):
    nombre: str
    sku: str = ""
    categoria: str = ""
    cantidad_stock: int = 0
    stock_minimo: int = 0
    precio_venta: Decimal | None = None
    precio_coste: Decimal | None = None
    proveedor: str = ""


class ArticuloCreate(ArticuloBase):
    pass


class ArticuloUpdate(BaseModel):
    nombre: str | None = None
    sku: str | None = None
    categoria: str | None = None
    cantidad_stock: int | None = None
    stock_minimo: int | None = None
    precio_venta: Decimal | None = None
    precio_coste: Decimal | None = None
    proveedor: str | None = None


class AjusteStockRequest(BaseModel):
    cantidad: int


class ArticuloResponse(ArticuloBase):
    id: str
    id_taller: str
    creado_en: datetime
    actualizado_en: datetime

    model_config = {"from_attributes": True}
