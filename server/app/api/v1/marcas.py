from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.marca_modelo import MarcaVehiculo, ModeloVehiculo
from app.schemas.marca_modelo import MarcaVehiculoResponse, ModeloVehiculoResponse

router = APIRouter()


@router.get("", response_model=list[MarcaVehiculoResponse])
def listar_marcas(
    tipo: str = Query("MOTO", description="MOTO o COCHE"),
    db: Session = Depends(get_db),
):
    marcas = (
        db.query(MarcaVehiculo)
        .filter(MarcaVehiculo.tipo_vehiculo == tipo)
        .order_by(MarcaVehiculo.nombre)
        .all()
    )
    return marcas


@router.get("/{marca_id}/modelos", response_model=list[ModeloVehiculoResponse])
def listar_modelos(marca_id: str, db: Session = Depends(get_db)):
    modelos = (
        db.query(ModeloVehiculo)
        .filter(ModeloVehiculo.id_marca == marca_id)
        .order_by(ModeloVehiculo.nombre)
        .all()
    )
    return modelos
