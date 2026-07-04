from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.cita import Cita
from app.models.usuario import Usuario
from app.schemas.cita import (
    CitaCreate,
    CitaUpdate,
    CitaResponse,
)
from app.schemas.common import Mensaje
from app.api.deps import get_current_user
from app.core.exceptions import NotFoundException

router = APIRouter()

ESTADOS_CITA = {"PENDIENTE", "CONFIRMADA", "EN_TALLER", "COMPLETADA", "CANCELADA"}


@router.get("", response_model=list[CitaResponse])
def listar_citas(
    fecha_desde: str | None = None,
    fecha_hasta: str | None = None,
    id_mecanico: str | None = None,
    estado: str | None = None,
    pagina: int = Query(1, ge=1),
    por_pagina: int = Query(50, ge=1, le=100),
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    query = db.query(Cita).filter(Cita.id_taller == user.id_taller)
    if fecha_desde:
        query = query.filter(Cita.fecha_hora >= fecha_desde)
    if fecha_hasta:
        query = query.filter(Cita.fecha_hora <= fecha_hasta)
    if id_mecanico:
        query = query.filter(Cita.id_mecanico == id_mecanico)
    if estado:
        query = query.filter(Cita.estado == estado)
    query = query.order_by(Cita.fecha_hora).offset(
        (pagina - 1) * por_pagina
    ).limit(por_pagina)
    return query.all()


@router.post("", response_model=CitaResponse, status_code=201)
def crear_cita(
    data: CitaCreate,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    cita = Cita(id_taller=user.id_taller, **data.model_dump())
    db.add(cita)
    db.commit()
    db.refresh(cita)
    return cita


@router.put("/{cita_id}", response_model=CitaResponse)
def actualizar_cita(
    cita_id: str,
    data: CitaUpdate,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    cita = db.query(Cita).filter(
        Cita.id == cita_id,
        Cita.id_taller == user.id_taller,
    ).first()
    if not cita:
        raise NotFoundException("Cita")
    for key, value in data.model_dump(exclude_unset=True).items():
        setattr(cita, key, value)
    db.commit()
    db.refresh(cita)
    return cita


@router.patch("/{cita_id}/estado", response_model=CitaResponse)
def cambiar_estado_cita(
    cita_id: str,
    data: dict,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    estado = data.get("estado")
    if estado not in ESTADOS_CITA:
        from fastapi import HTTPException
        raise HTTPException(
            status_code=400,
            detail=f"Estado inválido. Válidos: {', '.join(sorted(ESTADOS_CITA))}",
        )
    cita = db.query(Cita).filter(
        Cita.id == cita_id,
        Cita.id_taller == user.id_taller,
    ).first()
    if not cita:
        raise NotFoundException("Cita")
    cita.estado = estado
    db.commit()
    db.refresh(cita)
    return cita


@router.delete("/{cita_id}", response_model=Mensaje)
def eliminar_cita(
    cita_id: str,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    cita = db.query(Cita).filter(
        Cita.id == cita_id,
        Cita.id_taller == user.id_taller,
    ).first()
    if not cita:
        raise NotFoundException("Cita")
    db.delete(cita)
    db.commit()
    return Mensaje(detail="Cita eliminada")
