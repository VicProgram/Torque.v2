"""
Service for data migration from the legacy SQLite format to PostgreSQL.
Used during initial setup when importing data from the existing Android/Desktop backup.
"""
import sqlite3
from uuid import UUID
from datetime import datetime
from sqlalchemy.orm import Session

from app.models.taller import Taller
from app.models.cliente import Cliente
from app.models.vehiculo import Vehiculo
from app.models.orden_trabajo import OrdenTrabajo
from app.models.detalle_trabajo import DetalleTrabajo


def importar_desde_sqlite(
    db: Session,
    taller: Taller,
    ruta_sqlite: str,
):
    """Importa datos desde una base SQLite (formato Torque legacy) a PostgreSQL."""
    conn = sqlite3.connect(ruta_sqlite)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()

    cliente_default = Cliente(
        id_taller=taller.id,
        nombre="Cliente migrado",
        notas="Cliente creado durante migración desde SQLite",
    )
    db.add(cliente_default)
    db.flush()

    try:
        cursor.execute("SELECT * FROM motos")
        for row in cursor.fetchall():
            vehiculo = Vehiculo(
                id_taller=taller.id,
                id_cliente=cliente_default.id,
                matricula=row.get("matricula") or "",
                marca=row.get("marca") or "",
                modelo=row.get("modelo") or "",
                anio=int(row.get("anio") or 0),
                cilindrada=row.get("cilindrada") or "",
                caballos=row.get("caballos") or "",
                kilometros=int(row.get("kilometros") or 0),
                color=row.get("color") or "",
                tipo_vehiculo=row.get("tipoVehiculo") or "MOTO",
                notas_taller=row.get("notasTaller") or "",
            )
            db.add(vehiculo)
            db.flush()

        cursor.execute("SELECT * FROM mantenimientos")
        for row in cursor.fetchall():
            id_moto = row.get("idMoto")
            vehiculo = db.query(Vehiculo).filter(
                Vehiculo.id_taller == taller.id,
            ).offset(int(id_moto) - 1 if id_moto else 0).limit(1).first()

            if not vehiculo:
                continue

            orden = OrdenTrabajo(
                id_taller=taller.id,
                id_vehiculo=vehiculo.id,
                id_cliente=cliente_default.id,
                estado="COMPLETADO",
                descripcion_problema=row.get("nombreMantenimiento") or "",
            )
            db.add(orden)
            db.flush()

            detalle = DetalleTrabajo(
                id_orden_trabajo=orden.id,
                id_tipo_mantenimiento=int(row.get("tipoMantenimientoId") or 0),
                descripcion=row.get("nombreMantenimiento") or "",
                estado="COMPLETADO",
                precio_real=row.get("precio"),
                notas_mecanico=row.get("notas") or "",
            )
            db.add(detalle)

        db.commit()
    finally:
        conn.close()
