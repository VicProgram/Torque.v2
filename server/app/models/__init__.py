from app.models.base import Base
from app.models.taller import Taller
from app.models.usuario import Usuario
from app.models.cliente import Cliente
from app.models.vehiculo import Vehiculo
from app.models.orden_trabajo import OrdenTrabajo
from app.models.detalle_trabajo import DetalleTrabajo
from app.models.articulo import Articulo
from app.models.pieza_utilizada import PiezaUtilizada
from app.models.cita import Cita
from app.models.factura import Factura
from app.models.configuracion import ConfiguracionTaller
from app.models.archivo import Archivo

__all__ = [
    "Base",
    "Taller",
    "Usuario",
    "Cliente",
    "Vehiculo",
    "OrdenTrabajo",
    "DetalleTrabajo",
    "Articulo",
    "PiezaUtilizada",
    "Cita",
    "Factura",
    "ConfiguracionTaller",
    "Archivo",
]
