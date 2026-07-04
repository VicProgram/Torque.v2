from datetime import datetime, timezone, date, timedelta
from decimal import Decimal

from fastapi import APIRouter, Depends
from sqlalchemy import func
from sqlalchemy.orm import Session
from pydantic import BaseModel

from app.database import get_db
from app.models.orden_trabajo import OrdenTrabajo
from app.models.cita import Cita
from app.models.factura import Factura
from app.models.usuario import Usuario
from app.api.deps import get_current_user


class ResumenDashboard(BaseModel):
    ordenes_activas: int
    ordenes_presupuesto: int
    ordenes_en_curso: int
    ordenes_completadas_hoy: int
    proximas_citas: int
    citas_hoy: int
    facturas_pendientes: int
    ingresos_mes: Decimal


class OrdenActiva(BaseModel):
    id: str
    vehiculo: str
    problema: str
    estado: str
    mecanico: str | None = None


router = APIRouter()


@router.get("/resumen", response_model=ResumenDashboard)
def resumen(
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    id_taller = user.id_taller
    now = datetime.now(timezone.utc)
    today = date.today()
    today_start = datetime(today.year, today.month, today.day, tzinfo=timezone.utc)
    today_end = today_start + timedelta(days=1)
    month_start = datetime(today.year, today.month, 1, tzinfo=timezone.utc)
    next_month = month_start.replace(month=month_start.month % 12 + 1) if month_start.month < 12 else month_start.replace(year=month_start.year + 1, month=1)

    activas = db.query(func.count(OrdenTrabajo.id)).filter(
        OrdenTrabajo.id_taller == id_taller,
        OrdenTrabajo.estado.in_(["EN_CURSO"]),
    ).scalar() or 0

    presupuesto = db.query(func.count(OrdenTrabajo.id)).filter(
        OrdenTrabajo.id_taller == id_taller,
        OrdenTrabajo.estado == "PRESUPUESTO",
    ).scalar() or 0

    en_curso = db.query(func.count(OrdenTrabajo.id)).filter(
        OrdenTrabajo.id_taller == id_taller,
        OrdenTrabajo.estado == "EN_CURSO",
    ).scalar() or 0

    completadas_hoy = db.query(func.count(OrdenTrabajo.id)).filter(
        OrdenTrabajo.id_taller == id_taller,
        OrdenTrabajo.estado == "COMPLETADO",
        OrdenTrabajo.fecha_completado >= today_start,
        OrdenTrabajo.fecha_completado < today_end,
    ).scalar() or 0

    proximas = db.query(func.count(Cita.id)).filter(
        Cita.id_taller == id_taller,
        Cita.estado.in_(["PENDIENTE", "CONFIRMADA"]),
        Cita.fecha_hora >= now,
    ).scalar() or 0

    hoy = db.query(func.count(Cita.id)).filter(
        Cita.id_taller == id_taller,
        Cita.fecha_hora >= today_start,
        Cita.fecha_hora < today_end,
    ).scalar() or 0

    pendientes = db.query(func.count(Factura.id)).filter(
        Factura.id_taller == id_taller,
        Factura.estado == "EMITIDA",
    ).scalar() or 0

    ingresos = db.query(func.coalesce(func.sum(Factura.total), 0)).filter(
        Factura.id_taller == id_taller,
        Factura.estado == "PAGADA",
        Factura.fecha_emision >= month_start,
        Factura.fecha_emision < next_month,
    ).scalar() or Decimal("0")

    return ResumenDashboard(
        ordenes_activas=activas,
        ordenes_presupuesto=presupuesto,
        ordenes_en_curso=en_curso,
        ordenes_completadas_hoy=completadas_hoy,
        proximas_citas=proximas,
        citas_hoy=hoy,
        facturas_pendientes=pendientes,
        ingresos_mes=ingresos,
    )


@router.get("/ordenes-activas", response_model=list[OrdenActiva])
def ordenes_activas(
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    ordenes = db.query(OrdenTrabajo).filter(
        OrdenTrabajo.id_taller == user.id_taller,
        OrdenTrabajo.estado.in_(["PRESUPUESTO", "APROBADO", "EN_CURSO"]),
    ).order_by(OrdenTrabajo.creado_en.desc()).limit(20).all()

    result = []
    for o in ordenes:
        mecanico_nombre = None
        if o.id_mecanico_asignado:
            mecanico = db.query(Usuario).filter(
                Usuario.id == o.id_mecanico_asignado
            ).first()
            if mecanico:
                mecanico_nombre = mecanico.nombre

        vehiculo_str = f"{o.vehiculo.marca} {o.vehiculo.modelo}" if o.vehiculo else "?"
        result.append(OrdenActiva(
            id=str(o.id),
            vehiculo=vehiculo_str,
            problema=o.descripcion_problema or "",
            estado=o.estado,
            mecanico=mecanico_nombre,
        ))
    return result
