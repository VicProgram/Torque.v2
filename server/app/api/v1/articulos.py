from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.api.deps import get_current_user
from app.core.exceptions import NotFoundException
from app.database import get_db
from app.models.articulo import Articulo
from app.models.usuario import Usuario
from app.schemas.articulo import (
    AjusteStockRequest,
    ArticuloCreate,
    ArticuloResponse,
    ArticuloUpdate,
)
from app.schemas.common import Mensaje

router = APIRouter()


@router.get("", response_model=list[ArticuloResponse])
def listar_articulos(
    q: str = "",
    categoria: str | None = None,
    stock_bajo: bool = False,
    pagina: int = Query(1, ge=1),
    por_pagina: int = Query(20, ge=1, le=100),
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    query = db.query(Articulo).filter(Articulo.id_taller == user.id_taller)
    if q:
        query = query.filter(
            Articulo.nombre.ilike(f"%{q}%")
            | Articulo.sku.ilike(f"%{q}%")
        )
    if categoria:
        query = query.filter(Articulo.categoria == categoria)
    if stock_bajo:
        query = query.filter(Articulo.cantidad_stock <= Articulo.stock_minimo)
    query = query.order_by(Articulo.nombre).offset(
        (pagina - 1) * por_pagina
    ).limit(por_pagina)
    return query.all()


@router.post("", response_model=ArticuloResponse, status_code=201)
def crear_articulo(
    data: ArticuloCreate,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    articulo = Articulo(id_taller=user.id_taller, **data.model_dump())
    db.add(articulo)
    db.commit()
    db.refresh(articulo)
    return articulo


@router.get("/{articulo_id}", response_model=ArticuloResponse)
def obtener_articulo(
    articulo_id: str,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    articulo = db.query(Articulo).filter(
        Articulo.id == articulo_id,
        Articulo.id_taller == user.id_taller,
    ).first()
    if not articulo:
        raise NotFoundException("Artículo")
    return articulo


@router.put("/{articulo_id}", response_model=ArticuloResponse)
def actualizar_articulo(
    articulo_id: str,
    data: ArticuloUpdate,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    articulo = db.query(Articulo).filter(
        Articulo.id == articulo_id,
        Articulo.id_taller == user.id_taller,
    ).first()
    if not articulo:
        raise NotFoundException("Artículo")
    for key, value in data.model_dump(exclude_unset=True).items():
        setattr(articulo, key, value)
    db.commit()
    db.refresh(articulo)
    return articulo


@router.delete("/{articulo_id}", response_model=Mensaje)
def eliminar_articulo(
    articulo_id: str,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    articulo = db.query(Articulo).filter(
        Articulo.id == articulo_id,
        Articulo.id_taller == user.id_taller,
    ).first()
    if not articulo:
        raise NotFoundException("Artículo")
    db.delete(articulo)
    db.commit()
    return Mensaje(detail="Artículo eliminado")


@router.patch("/{articulo_id}/stock", response_model=ArticuloResponse)
def ajustar_stock(
    articulo_id: str,
    data: AjusteStockRequest,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    articulo = db.query(Articulo).filter(
        Articulo.id == articulo_id,
        Articulo.id_taller == user.id_taller,
    ).first()
    if not articulo:
        raise NotFoundException("Artículo")
    articulo.cantidad_stock += data.cantidad
    if articulo.cantidad_stock < 0:
        articulo.cantidad_stock = 0
    db.commit()
    db.refresh(articulo)
    return articulo
