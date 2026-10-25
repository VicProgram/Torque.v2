#!/usr/bin/env python
"""Seed a running Torque API with demo data. Safe to run more than once."""

import os

import requests

API_URL = os.getenv("TORQUE_API_URL", "http://localhost:8000/api/v1").rstrip("/")
DEMO_EMAIL = "admin@demo.com"
DEMO_PASSWORD = "demo1234"

CLIENTES = [
    {"nombre": "Juan Pérez", "telefono": "612345678"},
    {"nombre": "María García", "telefono": "611111111"},
]

ARTICULOS = [
    {"nombre": "Aceite 10W40", "sku": "ACE-001", "cantidad_stock": 20, "precio_venta": 12.5},
    {"nombre": "Filtro Aceite", "sku": "FIL-001", "cantidad_stock": 15, "precio_venta": 8.0},
]


def _login(session: requests.Session) -> requests.Response:
    return session.post(
        f"{API_URL}/auth/login",
        json={"email": DEMO_EMAIL, "password": DEMO_PASSWORD},
    )


def _get_token(session: requests.Session) -> str:
    response = _login(session)
    if response.status_code == 200:
        return response.json()["access_token"]

    response = session.post(
        f"{API_URL}/auth/registro-taller",
        json={
            "nombre_taller": "Taller Demo",
            "nombre_admin": "Admin",
            "email_admin": DEMO_EMAIL,
            "password": DEMO_PASSWORD,
        },
    )
    if response.status_code == 200:
        return response.json()["access_token"]

    # The workshop already exists: log in instead of reusing the failed response.
    response = _login(session)
    if response.status_code != 200:
        raise SystemExit(
            f"No se pudo autenticar como {DEMO_EMAIL}: {response.status_code} {response.text}"
        )
    return response.json()["access_token"]


def _ensure(
    session: requests.Session,
    headers: dict[str, str],
    path: str,
    item: dict,
    key: str,
) -> bool:
    response = session.get(f"{API_URL}{path}", headers=headers, params={"q": item[key]})
    response.raise_for_status()
    if any(record.get(key) == item[key] for record in response.json()):
        return False

    response = session.post(f"{API_URL}{path}", headers=headers, json=item)
    response.raise_for_status()
    return True


def main() -> None:
    session = requests.Session()
    try:
        token = _get_token(session)
        headers = {"Authorization": f"Bearer {token}"}

        created = 0
        for cliente in CLIENTES:
            created += _ensure(session, headers, "/clientes", cliente, "nombre")
        for articulo in ARTICULOS:
            created += _ensure(session, headers, "/articulos", articulo, "sku")
    except requests.RequestException as exc:
        raise SystemExit(f"No se pudo conectar con {API_URL}: {exc}") from exc

    print(f"Datos demo listos en {API_URL} ({created} registros nuevos).")


if __name__ == "__main__":
    main()
