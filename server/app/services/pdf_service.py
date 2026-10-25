from decimal import Decimal
from io import BytesIO

from reportlab.lib import colors
from reportlab.lib.enums import TA_RIGHT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.platypus import (
    Paragraph,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)


def generar_presupuesto_pdf(
    numero_orden: str,
    taller_nombre: str,
    taller_direccion: str,
    taller_telefono: str,
    cliente_nombre: str,
    vehiculo_info: str,
    detalles: list[dict],
    presupuesto_total: Decimal,
) -> BytesIO:
    buffer = BytesIO()
    doc = SimpleDocTemplate(buffer, pagesize=A4)
    styles = getSampleStyleSheet()
    elements = []

    elements.append(Paragraph(f"PRESUPUESTO #{numero_orden}", styles["Title"]))
    elements.append(Spacer(1, 6 * mm))

    elements.append(Paragraph(f"<b>{taller_nombre}</b>", styles["Normal"]))
    if taller_direccion:
        elements.append(Paragraph(taller_direccion, styles["Normal"]))
    if taller_telefono:
        elements.append(Paragraph(f"Tel: {taller_telefono}", styles["Normal"]))
    elements.append(Spacer(1, 6 * mm))

    elements.append(Paragraph(f"<b>Cliente:</b> {cliente_nombre}", styles["Normal"]))
    elements.append(Paragraph(f"<b>Vehículo:</b> {vehiculo_info}", styles["Normal"]))
    elements.append(Spacer(1, 6 * mm))

    data = [["#", "Descripción", "Precio"]]
    for i, det in enumerate(detalles, 1):
        precio = f"{det.get('precio_estimado', 0):.2f} €"
        data.append([str(i), det.get("descripcion", ""), precio])

    data.append(["", "TOTAL", f"{presupuesto_total:.2f} €"])

    table = Table(data, colWidths=[30, 350, 100])
    table.setStyle(TableStyle([
        ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
        ("FONTSIZE", (0, 0), (-1, -1), 10),
        ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#135BEC")),
        ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
        ("ALIGN", (2, 0), (2, -1), TA_RIGHT),
        ("FONTNAME", (0, -1), (-1, -1), "Helvetica-Bold"),
        ("LINEBELOW", (0, -1), (-1, -1), 2, colors.HexColor("#135BEC")),
        ("GRID", (0, 0), (-1, -2), 0.5, colors.grey),
        ("TOPPADDING", (0, 0), (-1, -1), 6),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
    ]))
    elements.append(table)

    doc.build(elements)
    buffer.seek(0)
    return buffer
