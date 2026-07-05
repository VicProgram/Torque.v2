#!/usr/bin/env python
"""Seed the database with default vehicle brands and models."""

import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from app.database import engine, SessionLocal, Base
from app.models.marca_modelo import MarcaVehiculo, ModeloVehiculo
from app.models.base import Base as AppBase
from app.data.marcas import MARCAS_MOTO, MARCAS_COCHE


def seed():
    AppBase.metadata.create_all(bind=engine)

    db = SessionLocal()

    existing = db.query(MarcaVehiculo).first()
    if existing:
        print("Brand/model data already seeded. Skipping.")
        db.close()
        return

    for tipo, marcas in [("MOTO", MARCAS_MOTO), ("COCHE", MARCAS_COCHE)]:
        for nombre_marca, modelos in sorted(marcas.items()):
            marca = MarcaVehiculo(nombre=nombre_marca, tipo_vehiculo=tipo)
            db.add(marca)
            db.flush()

            for nombre_modelo in modelos:
                modelo = ModeloVehiculo(
                    id_marca=marca.id, nombre=nombre_modelo
                )
                db.add(modelo)

    db.commit()
    db.close()

    total_marcas = len(MARCAS_MOTO) + len(MARCAS_COCHE)
    total_modelos = sum(len(v) for v in MARCAS_MOTO.values()) + sum(
        len(v) for v in MARCAS_COCHE.values()
    )
    print(f"Seeded {total_marcas} brands and {total_modelos} models.")


if __name__ == "__main__":
    seed()
