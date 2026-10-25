from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.api.deps import get_current_user, require_taller_record
from app.core.exceptions import NotFoundException
from app.database import get_db
from app.models.cliente import Cliente
from app.models.usuario import Usuario
from app.models.vehiculo import Vehiculo
from app.schemas.common import Mensaje
from app.schemas.vehiculo import (
    BuscarMatriculaResponse,
    VehiculoCreate,
    VehiculoResponse,
    VehiculoUpdate,
)

router = APIRouter()


@router.get("", response_model=list[VehiculoResponse])
def listar_vehiculos(
    q: str = "",
    id_cliente: str | None = None,
    pagina: int = Query(1, ge=1),
    por_pagina: int = Query(20, ge=1, le=100),
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    query = db.query(Vehiculo).filter(Vehiculo.id_taller == user.id_taller)
    if q:
        query = query.filter(
            Vehiculo.matricula.ilike(f"%{q}%")
            | Vehiculo.marca.ilike(f"%{q}%")
            | Vehiculo.modelo.ilike(f"%{q}%")
        )
    if id_cliente:
        query = query.filter(Vehiculo.id_cliente == id_cliente)
    query = query.order_by(Vehiculo.marca).offset(
        (pagina - 1) * por_pagina
    ).limit(por_pagina)
    return query.all()


@router.post("", response_model=VehiculoResponse, status_code=201)
def crear_vehiculo(
    data: VehiculoCreate,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    require_taller_record(db, Cliente, data.id_cliente, user.id_taller, "Cliente")
    vehiculo = Vehiculo(id_taller=user.id_taller, **data.model_dump())
    db.add(vehiculo)
    db.commit()
    db.refresh(vehiculo)
    return vehiculo


@router.get("/buscar-matricula", response_model=list[BuscarMatriculaResponse])
def buscar_por_matricula(
    q: str = Query("", min_length=1),
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    vehiculos = db.query(Vehiculo).filter(
        Vehiculo.matricula.ilike(f"%{q}%"),
        Vehiculo.id_taller == user.id_taller,
    ).limit(10).all()

    result = []
    for v in vehiculos:
        cliente_nombre = ""
        cliente_id = None
        if v.id_cliente:
            cliente = db.query(Cliente).filter(
                Cliente.id == v.id_cliente,
                Cliente.id_taller == user.id_taller,
            ).first()
            if cliente:
                cliente_nombre = cliente.nombre
                cliente_id = str(cliente.id)
        result.append(BuscarMatriculaResponse(
            id=str(v.id),
            matricula=v.matricula,
            marca=v.marca,
            modelo=v.modelo,
            cliente_nombre=cliente_nombre,
            cliente_id=cliente_id,
        ))
    return result


@router.get("/{vehiculo_id}", response_model=VehiculoResponse)
def obtener_vehiculo(
    vehiculo_id: str,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    vehiculo = db.query(Vehiculo).filter(
        Vehiculo.id == vehiculo_id,
        Vehiculo.id_taller == user.id_taller,
    ).first()
    if not vehiculo:
        raise NotFoundException("Vehículo")
    return vehiculo


@router.put("/{vehiculo_id}", response_model=VehiculoResponse)
def actualizar_vehiculo(
    vehiculo_id: str,
    data: VehiculoUpdate,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    vehiculo = db.query(Vehiculo).filter(
        Vehiculo.id == vehiculo_id,
        Vehiculo.id_taller == user.id_taller,
    ).first()
    if not vehiculo:
        raise NotFoundException("Vehículo")
    require_taller_record(db, Cliente, data.id_cliente, user.id_taller, "Cliente")
    for key, value in data.model_dump(exclude_unset=True).items():
        setattr(vehiculo, key, value)
    db.commit()
    db.refresh(vehiculo)
    return vehiculo


@router.delete("/{vehiculo_id}", response_model=Mensaje)
def eliminar_vehiculo(
    vehiculo_id: str,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    vehiculo = db.query(Vehiculo).filter(
        Vehiculo.id == vehiculo_id,
        Vehiculo.id_taller == user.id_taller,
    ).first()
    if not vehiculo:
        raise NotFoundException("Vehículo")
    db.delete(vehiculo)
    db.commit()
    return Mensaje(detail="Vehículo eliminado")
