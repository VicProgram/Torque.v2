from datetime import UTC, datetime

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.api.deps import get_current_user, require_taller_record
from app.core.exceptions import NotFoundException
from app.core.websocket import manager
from app.database import get_db
from app.models.cliente import Cliente
from app.models.detalle_trabajo import DetalleTrabajo
from app.models.orden_trabajo import OrdenTrabajo
from app.models.usuario import Usuario
from app.models.vehiculo import Vehiculo
from app.schemas.common import Mensaje
from app.schemas.orden_trabajo import (
    CambioEstadoRequest,
    DetalleTrabajoCreate,
    DetalleTrabajoResponse,
    DetalleTrabajoUpdate,
    OrdenTrabajoCreate,
    OrdenTrabajoResponse,
    OrdenTrabajoUpdate,
)

router = APIRouter()


ESTADOS_VALIDOS = {
    "PRESUPUESTO", "APROBADO", "EN_CURSO", "COMPLETADO", "FACTURADO", "CANCELADO"
}


def _orden_to_response(orden: OrdenTrabajo, db: Session | None = None) -> OrdenTrabajoResponse:
    detalles = [
        DetalleTrabajoResponse(
            id=str(d.id),
            id_orden_trabajo=str(d.id_orden_trabajo),
            id_tipo_mantenimiento=d.id_tipo_mantenimiento,
            descripcion=d.descripcion,
            estado=d.estado,
            precio_estimado=d.precio_estimado,
            precio_real=d.precio_real,
            tiempo_estimado=d.tiempo_estimado,
            tiempo_real=d.tiempo_real,
            notas_mecanico=d.notas_mecanico,
            orden=d.orden,
        )
        for d in orden.detalles
    ]

    vehiculo_matricula = ""
    vehiculo_marca = ""
    vehiculo_modelo = ""
    cliente_nombre = ""
    mecanico_nombre = ""

    if db:
        if orden.id_vehiculo:
            v = db.query(Vehiculo).filter(
                Vehiculo.id == orden.id_vehiculo,
                Vehiculo.id_taller == orden.id_taller,
            ).first()
            if v:
                vehiculo_matricula = v.matricula or ""
                vehiculo_marca = v.marca or ""
                vehiculo_modelo = v.modelo or ""
        if orden.id_cliente:
            c = db.query(Cliente).filter(
                Cliente.id == orden.id_cliente,
                Cliente.id_taller == orden.id_taller,
            ).first()
            if c:
                cliente_nombre = c.nombre or ""
        if orden.id_mecanico_asignado:
            m = db.query(Usuario).filter(
                Usuario.id == orden.id_mecanico_asignado,
                Usuario.id_taller == orden.id_taller,
            ).first()
            if m:
                mecanico_nombre = m.nombre or ""

    return OrdenTrabajoResponse(
        id=str(orden.id),
        id_taller=str(orden.id_taller),
        id_vehiculo=str(orden.id_vehiculo),
        id_cliente=str(orden.id_cliente) if orden.id_cliente else None,
        id_mecanico_asignado=str(orden.id_mecanico_asignado) if orden.id_mecanico_asignado else None,
        vehiculo_matricula=vehiculo_matricula,
        vehiculo_marca=vehiculo_marca,
        vehiculo_modelo=vehiculo_modelo,
        cliente_nombre=cliente_nombre,
        mecanico_nombre=mecanico_nombre,
        estado=orden.estado,
        descripcion_problema=orden.descripcion_problema or "",
        diagnostico=orden.diagnostico or "",
        kilometros_entrada=orden.kilometros_entrada,
        presupuesto_total=orden.presupuesto_total,
        total_real=orden.total_real,
        fecha_salida_estimada=orden.fecha_salida_estimada,
        notas_internas=orden.notas_internas or "",
        notas_cliente=orden.notas_cliente or "",
        fecha_entrada=orden.fecha_entrada,
        fecha_completado=orden.fecha_completado,
        creado_en=orden.creado_en,
        actualizado_en=orden.actualizado_en,
        detalles=detalles,
    )


@router.get("", response_model=list[OrdenTrabajoResponse])
def listar_ordenes(
    estado: str | None = None,
    id_mecanico: str | None = None,
    id_vehiculo: str | None = None,
    pagina: int = Query(1, ge=1),
    por_pagina: int = Query(20, ge=1, le=100),
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    query = db.query(OrdenTrabajo).filter(
        OrdenTrabajo.id_taller == user.id_taller
    )
    if estado:
        query = query.filter(OrdenTrabajo.estado == estado)
    if id_mecanico:
        query = query.filter(OrdenTrabajo.id_mecanico_asignado == id_mecanico)
    if id_vehiculo:
        query = query.filter(OrdenTrabajo.id_vehiculo == id_vehiculo)
    query = query.order_by(OrdenTrabajo.creado_en.desc()).offset(
        (pagina - 1) * por_pagina
    ).limit(por_pagina)
    return [_orden_to_response(o, db) for o in query.all()]


@router.post("", response_model=OrdenTrabajoResponse, status_code=201)
def crear_orden(
    data: OrdenTrabajoCreate,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    require_taller_record(db, Vehiculo, data.id_vehiculo, user.id_taller, "Vehículo")
    require_taller_record(db, Cliente, data.id_cliente, user.id_taller, "Cliente")
    require_taller_record(
        db, Usuario, data.id_mecanico_asignado, user.id_taller, "Mecánico"
    )
    orden = OrdenTrabajo(
        id_taller=user.id_taller,
        fecha_entrada=datetime.now(UTC),
        **data.model_dump(),
    )
    db.add(orden)
    db.commit()
    db.refresh(orden)
    return _orden_to_response(orden, db)


@router.get("/{orden_id}", response_model=OrdenTrabajoResponse)
def obtener_orden(
    orden_id: str,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    orden = db.query(OrdenTrabajo).filter(
        OrdenTrabajo.id == orden_id,
        OrdenTrabajo.id_taller == user.id_taller,
    ).first()
    if not orden:
        raise NotFoundException("Orden de trabajo")
    return _orden_to_response(orden, db)


@router.put("/{orden_id}", response_model=OrdenTrabajoResponse)
def actualizar_orden(
    orden_id: str,
    data: OrdenTrabajoUpdate,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    orden = db.query(OrdenTrabajo).filter(
        OrdenTrabajo.id == orden_id,
        OrdenTrabajo.id_taller == user.id_taller,
    ).first()
    if not orden:
        raise NotFoundException("Orden de trabajo")
    require_taller_record(db, Vehiculo, data.id_vehiculo, user.id_taller, "Vehículo")
    require_taller_record(db, Cliente, data.id_cliente, user.id_taller, "Cliente")
    require_taller_record(
        db, Usuario, data.id_mecanico_asignado, user.id_taller, "Mecánico"
    )
    for key, value in data.model_dump(exclude_unset=True).items():
        setattr(orden, key, value)
    db.commit()
    db.refresh(orden)
    return _orden_to_response(orden, db)


@router.patch("/{orden_id}/estado", response_model=OrdenTrabajoResponse)
def cambiar_estado_orden(
    orden_id: str,
    data: CambioEstadoRequest,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if data.estado not in ESTADOS_VALIDOS:
        from fastapi import HTTPException
        raise HTTPException(
            status_code=400,
            detail=f"Estado inválido. Válidos: {', '.join(sorted(ESTADOS_VALIDOS))}",
        )

    orden = db.query(OrdenTrabajo).filter(
        OrdenTrabajo.id == orden_id,
        OrdenTrabajo.id_taller == user.id_taller,
    ).first()
    if not orden:
        raise NotFoundException("Orden de trabajo")

    old_estado = orden.estado
    orden.estado = data.estado
    if data.estado == "COMPLETADO":
        orden.fecha_completado = datetime.now(UTC)
    db.commit()
    db.refresh(orden)

    import asyncio
    try:
        loop = asyncio.get_event_loop()
        if loop.is_running():
            loop.create_task(
                manager.broadcast_to_taller(
                    str(user.id_taller),
                    {
                        "type": "orden.estado.cambiado",
                        "orden_id": str(orden.id),
                        "old_estado": old_estado,
                        "new_estado": data.estado,
                    },
                )
            )
    except RuntimeError:
        pass

    return _orden_to_response(orden, db)


@router.post("/{orden_id}/detalles", response_model=DetalleTrabajoResponse, status_code=201)
def crear_detalle(
    orden_id: str,
    data: DetalleTrabajoCreate,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    orden = db.query(OrdenTrabajo).filter(
        OrdenTrabajo.id == orden_id,
        OrdenTrabajo.id_taller == user.id_taller,
    ).first()
    if not orden:
        raise NotFoundException("Orden de trabajo")
    detalle = DetalleTrabajo(id_orden_trabajo=orden.id, **data.model_dump())
    db.add(detalle)
    db.commit()
    db.refresh(detalle)
    return DetalleTrabajoResponse(
        id=str(detalle.id),
        id_orden_trabajo=str(detalle.id_orden_trabajo),
        id_tipo_mantenimiento=detalle.id_tipo_mantenimiento,
        descripcion=detalle.descripcion,
        estado=detalle.estado,
        precio_estimado=detalle.precio_estimado,
        precio_real=detalle.precio_real,
        tiempo_estimado=detalle.tiempo_estimado,
        tiempo_real=detalle.tiempo_real,
        notas_mecanico=detalle.notas_mecanico or "",
        orden=detalle.orden,
    )


@router.patch("/detalles/{detalle_id}", response_model=DetalleTrabajoResponse)
def actualizar_detalle(
    detalle_id: str,
    data: DetalleTrabajoUpdate,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    detalle = db.query(DetalleTrabajo).join(OrdenTrabajo).filter(
        DetalleTrabajo.id == detalle_id,
        OrdenTrabajo.id_taller == user.id_taller,
    ).first()
    if not detalle:
        raise NotFoundException("Detalle de trabajo")
    for key, value in data.model_dump(exclude_unset=True).items():
        setattr(detalle, key, value)
    db.commit()
    db.refresh(detalle)
    return DetalleTrabajoResponse(
        id=str(detalle.id),
        id_orden_trabajo=str(detalle.id_orden_trabajo),
        id_tipo_mantenimiento=detalle.id_tipo_mantenimiento,
        descripcion=detalle.descripcion,
        estado=detalle.estado,
        precio_estimado=detalle.precio_estimado,
        precio_real=detalle.precio_real,
        tiempo_estimado=detalle.tiempo_estimado,
        tiempo_real=detalle.tiempo_real,
        notas_mecanico=detalle.notas_mecanico or "",
        orden=detalle.orden,
    )


@router.delete("/detalles/{detalle_id}", response_model=Mensaje)
def eliminar_detalle(
    detalle_id: str,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    detalle = db.query(DetalleTrabajo).join(OrdenTrabajo).filter(
        DetalleTrabajo.id == detalle_id,
        OrdenTrabajo.id_taller == user.id_taller,
    ).first()
    if not detalle:
        raise NotFoundException("Detalle de trabajo")
    db.delete(detalle)
    db.commit()
    return Mensaje(detail="Detalle eliminado")
