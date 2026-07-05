from pydantic import BaseModel


class MarcaVehiculoResponse(BaseModel):
    id: str
    nombre: str
    tipo_vehiculo: str

    class Config:
        from_attributes = True


class ModeloVehiculoResponse(BaseModel):
    id: str
    nombre: str
    id_marca: str

    class Config:
        from_attributes = True
