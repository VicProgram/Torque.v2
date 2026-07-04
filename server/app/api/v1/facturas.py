from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.factura import Factura
from app.models.usuario import Usuario
from app.schemas.factura import (
    FacturaCreate,
    FacturaUpdate,
    FacturaResponse,
)
from app.schemas.common import Mensaje
from app.api.deps import get_current_user
from app.core.exceptions import NotFoundException

router = APIRouter()


@router.get("", response_model=list[FacturaResponse])
def listar_facturas(
    estado: str | None = None,
    pagina: int = Query(1, ge=1),
    por_pagina: int = Query(20, ge=1, le=100),
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    query = db.query(Factura).filter(Factura.id_taller == user.id_taller)
    if estado:
        query = query.filter(Factura.estado == estado)
    query = query.order_by(Factura.fecha_emision.desc()).offset(
        (pagina - 1) * por_pagina
    ).limit(por_pagina)
    return query.all()


@router.post("", response_model=FacturaResponse, status_code=201)
def crear_factura(
    data: FacturaCreate,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    factura = Factura(id_taller=user.id_taller, **data.model_dump())
    db.add(factura)
    db.commit()
    db.refresh(factura)
    return factura


@router.get("/{factura_id}", response_model=FacturaResponse)
def obtener_factura(
    factura_id: str,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    factura = db.query(Factura).filter(
        Factura.id == factura_id,
        Factura.id_taller == user.id_taller,
    ).first()
    if not factura:
        raise NotFoundException("Factura")
    return factura


@router.patch("/{factura_id}", response_model=FacturaResponse)
def actualizar_factura(
    factura_id: str,
    data: FacturaUpdate,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    factura = db.query(Factura).filter(
        Factura.id == factura_id,
        Factura.id_taller == user.id_taller,
    ).first()
    if not factura:
        raise NotFoundException("Factura")
    for key, value in data.model_dump(exclude_unset=True).items():
        setattr(factura, key, value)
    db.commit()
    db.refresh(factura)
    return factura
