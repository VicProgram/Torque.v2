from fastapi import APIRouter

from app.api.v1 import auth, clientes, vehiculos, ordenes, articulos, citas, facturas, dashboard, marcas

api_router = APIRouter()

api_router.include_router(auth.router, prefix="/auth", tags=["Autenticación"])
api_router.include_router(clientes.router, prefix="/clientes", tags=["Clientes"])
api_router.include_router(
    vehiculos.router, prefix="/vehiculos", tags=["Vehículos"]
)
api_router.include_router(
    ordenes.router, prefix="/ordenes", tags=["Órdenes de Trabajo"]
)
api_router.include_router(articulos.router, prefix="/articulos", tags=["Inventario"])
api_router.include_router(citas.router, prefix="/citas", tags=["Citas / Agenda"])
api_router.include_router(facturas.router, prefix="/facturas", tags=["Facturas"])
api_router.include_router(
    dashboard.router, prefix="/dashboard", tags=["Dashboard"]
)
api_router.include_router(
    marcas.router, prefix="/marcas", tags=["Marcas y Modelos"]
)
