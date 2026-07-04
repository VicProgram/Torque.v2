from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.cliente import Cliente
from app.models.usuario import Usuario
from app.models.vehiculo import Vehiculo
from app.schemas.cliente import (
    ClienteCreate,
    ClienteUpdate,
    ClienteResponse,
)
from app.schemas.vehiculo import VehiculoResponse
from app.schemas.common import Mensaje
from app.api.deps import get_current_user
from app.core.exceptions import NotFoundException

router = APIRouter()


def _filter_by_taller(query, taller_id):
    return query.filter(Cliente.id_taller == taller_id)


@router.get("", response_model=list[ClienteResponse])
def listar_clientes(
    q: str = "",
    pagina: int = Query(1, ge=1),
    por_pagina: int = Query(20, ge=1, le=100),
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    query = db.query(Cliente).filter(Cliente.id_taller == user.id_taller)
    if q:
        query = query.filter(
            Cliente.nombre.ilike(f"%{q}%")
            | Cliente.telefono.ilike(f"%{q}%")
            | Cliente.email.ilike(f"%{q}%")
        )
    query = query.order_by(Cliente.nombre).offset(
        (pagina - 1) * por_pagina
    ).limit(por_pagina)
    return query.all()


@router.post("", response_model=ClienteResponse, status_code=201)
def crear_cliente(
    data: ClienteCreate,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    cliente = Cliente(id_taller=user.id_taller, **data.model_dump())
    db.add(cliente)
    db.commit()
    db.refresh(cliente)
    return cliente


@router.get("/{cliente_id}", response_model=ClienteResponse)
def obtener_cliente(
    cliente_id: str,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    cliente = db.query(Cliente).filter(
        Cliente.id == cliente_id,
        Cliente.id_taller == user.id_taller,
    ).first()
    if not cliente:
        raise NotFoundException("Cliente")
    return cliente


@router.put("/{cliente_id}", response_model=ClienteResponse)
def actualizar_cliente(
    cliente_id: str,
    data: ClienteUpdate,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    cliente = db.query(Cliente).filter(
        Cliente.id == cliente_id,
        Cliente.id_taller == user.id_taller,
    ).first()
    if not cliente:
        raise NotFoundException("Cliente")
    for key, value in data.model_dump(exclude_unset=True).items():
        setattr(cliente, key, value)
    db.commit()
    db.refresh(cliente)
    return cliente


@router.delete("/{cliente_id}", response_model=Mensaje)
def eliminar_cliente(
    cliente_id: str,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    cliente = db.query(Cliente).filter(
        Cliente.id == cliente_id,
        Cliente.id_taller == user.id_taller,
    ).first()
    if not cliente:
        raise NotFoundException("Cliente")
    db.delete(cliente)
    db.commit()
    return Mensaje(detail="Cliente eliminado")


@router.get("/{cliente_id}/vehiculos", response_model=list[VehiculoResponse])
def listar_vehiculos_cliente(
    cliente_id: str,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    vehiculos = db.query(Vehiculo).filter(
        Vehiculo.id_cliente == cliente_id,
        Vehiculo.id_taller == user.id_taller,
    ).all()
    return vehiculos
