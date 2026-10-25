from datetime import date, datetime


def format_fecha(fecha_str: str | None, fmt: str = "%d/%m/%Y") -> str:
    if not fecha_str:
        return ""
    try:
        dt = datetime.fromisoformat(fecha_str)
        return dt.strftime(fmt)
    except (ValueError, TypeError):
        return fecha_str[:10] if fecha_str else ""


def format_fecha_date(d: date | None, fmt: str = "%d/%m/%Y") -> str:
    if not d:
        return ""
    return d.strftime(fmt)


def format_decimal(val) -> str:
    if val is None:
        return ""
    return f"{float(val):.2f} €"


ESTADOS_ORDEN = [
    "PRESUPUESTO",
    "APROBADO",
    "EN_CURSO",
    "COMPLETADO",
    "FACTURADO",
    "CANCELADO",
]

ESTADOS_CITA = [
    "PENDIENTE",
    "CONFIRMADA",
    "EN_TALLER",
    "COMPLETADA",
    "CANCELADA",
]

ESTADOS_FACTURA = ["EMITIDA", "PAGADA", "ANULADA", "VENCIDA"]

COLOR_ESTADOS = {
    "PRESUPUESTO": "#EAB308",
    "APROBADO": "#22C55E",
    "EN_CURSO": "#3B82F6",
    "COMPLETADO": "#22C55E",
    "FACTURADO": "#8B5CF6",
    "CANCELADO": "#EF4444",
    "PENDIENTE": "#EAB308",
    "CONFIRMADA": "#22C55E",
    "EN_TALLER": "#3B82F6",
    "COMPLETADA": "#22C55E",
    "EMITIDA": "#EAB308",
    "PAGADA": "#22C55E",
    "ANULADA": "#EF4444",
    "VENCIDA": "#EF4444",
}
