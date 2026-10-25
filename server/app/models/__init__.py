from app.models.archivo import Archivo
from app.models.articulo import Articulo
from app.models.base import Base
from app.models.cita import Cita
from app.models.cliente import Cliente
from app.models.configuracion import ConfiguracionTaller
from app.models.detalle_trabajo import DetalleTrabajo
from app.models.factura import Factura
from app.models.marca_modelo import MarcaVehiculo, ModeloVehiculo
from app.models.orden_trabajo import OrdenTrabajo
from app.models.pieza_utilizada import PiezaUtilizada
from app.models.taller import Taller
from app.models.usuario import Usuario
from app.models.vehiculo import Vehiculo

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
    "MarcaVehiculo",
    "ModeloVehiculo",
]
