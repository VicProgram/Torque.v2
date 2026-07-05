from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.factura import Factura
from app.models.orden_trabajo import OrdenTrabajo
from app.models.vehiculo import Vehiculo
from app.models.cliente import Cliente
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


def _factura_to_response(factura: Factura, db: Session) -> FacturaResponse:
    orden_vehiculo = ""
    orden_cliente = ""
    if factura.id_orden_trabajo:
        orden = db.query(OrdenTrabajo).filter(
            OrdenTrabajo.id == factura.id_orden_trabajo
        ).first()
        if orden:
            if orden.id_vehiculo:
                v = db.query(Vehiculo).filter(Vehiculo.id == orden.id_vehiculo).first()
                if v:
                    orden_vehiculo = f"{v.matricula} {v.marca} {v.modelo}".strip()
            if orden.id_cliente:
                c = db.query(Cliente).filter(Cliente.id == orden.id_cliente).first()
                if c:
                    orden_cliente = c.nombre or ""
    return FacturaResponse(
        id=factura.id,
        id_taller=factura.id_taller,
        id_orden_trabajo=factura.id_orden_trabajo,
        orden_vehiculo=orden_vehiculo,
        orden_cliente=orden_cliente,
        numero_factura=factura.numero_factura,
        fecha_emision=factura.fecha_emision,
        fecha_vencimiento=factura.fecha_vencimiento,
        base_imponible=factura.base_imponible,
        iva=factura.iva,
        total=factura.total,
        metodo_pago=factura.metodo_pago or "",
        notas=factura.notas or "",
        estado=factura.estado,
        pdf_url=factura.pdf_url or "",
        creado_en=factura.creado_en,
    )


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
    return [_factura_to_response(f, db) for f in query.all()]


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
    return _factura_to_response(factura, db)


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
    return _factura_to_response(factura, db)


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
    return _factura_to_response(factura, db)
