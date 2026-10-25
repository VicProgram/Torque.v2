from datetime import UTC, datetime

from sqlalchemy.orm import Session

from app.core.exceptions import NotFoundException
from app.models.detalle_trabajo import DetalleTrabajo
from app.models.orden_trabajo import OrdenTrabajo

ESTADOS_VALIDOS = {
    "PRESUPUESTO", "APROBADO", "EN_CURSO", "COMPLETADO", "FACTURADO", "CANCELADO"
}


def get_orden(db: Session, orden_id: str, taller_id: str) -> OrdenTrabajo:
    orden = db.query(OrdenTrabajo).filter(
        OrdenTrabajo.id == orden_id,
        OrdenTrabajo.id_taller == taller_id,
    ).first()
    if not orden:
        raise NotFoundException("Orden de trabajo")
    return orden


def cambiar_estado(
    db: Session, orden: OrdenTrabajo, nuevo_estado: str
) -> OrdenTrabajo:
    if nuevo_estado not in ESTADOS_VALIDOS:
        raise ValueError(
            f"Estado inválido. Válidos: {', '.join(sorted(ESTADOS_VALIDOS))}"
        )
    orden.estado = nuevo_estado
    if nuevo_estado == "COMPLETADO":
        orden.fecha_completado = datetime.now(UTC)
    db.commit()
    db.refresh(orden)
    return orden


def add_detalle(
    db: Session, orden: OrdenTrabajo, data: dict
) -> DetalleTrabajo:
    detalle = DetalleTrabajo(id_orden_trabajo=orden.id, **data)
    db.add(detalle)
    db.commit()
    db.refresh(detalle)
    return detalle
