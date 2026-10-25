from typing import Generic, TypeVar

from pydantic import BaseModel

T = TypeVar("T")


class Mensaje(BaseModel):
    detail: str


class Paginacion(BaseModel):
    pagina: int = 1
    por_pagina: int = 20
    total: int = 0
    total_paginas: int = 0


class RespuestaPaginada(BaseModel, Generic[T]):
    data: list[T]
    paginacion: Paginacion


class RespuestaUnica(BaseModel, Generic[T]):
    data: T
