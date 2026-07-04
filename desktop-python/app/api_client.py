from __future__ import annotations

from typing import Any
import httpx

from app.config import CONFIG


class ApiClient:
    def __init__(self):
        self._client: httpx.Client | None = None
        self._token: str | None = None

    @property
    def client(self) -> httpx.Client:
        if self._client is None:
            self._client = httpx.Client(base_url=CONFIG.api_url, timeout=30)
        return self._client

    @property
    def headers(self) -> dict:
        h = {"Content-Type": "application/json"}
        if self._token:
            h["Authorization"] = f"Bearer {self._token}"
        return h

    def set_token(self, token: str):
        self._token = token

    def clear_token(self):
        self._token = None

    def _request(self, method: str, path: str, **kwargs) -> httpx.Response:
        kwargs.setdefault("headers", {}).update(self.headers)
        return self.client.request(method, path, **kwargs)

    def login(self, email: str, password: str) -> dict | None:
        try:
            resp = self._request(
                "POST",
                "/auth/login",
                json={"email": email, "password": password},
            )
            if resp.status_code == 200:
                data = resp.json()
                self.set_token(data["access_token"])
                return data
            return None
        except httpx.RequestError:
            return None

    def login_pin(self, pin: str) -> dict | None:
        try:
            resp = self._request(
                "POST", "/auth/login-pin", json={"pin_code": pin}
            )
            if resp.status_code == 200:
                data = resp.json()
                self.set_token(data["access_token"])
                return data
            return None
        except httpx.RequestError:
            return None

    def get_me(self) -> dict | None:
        try:
            resp = self._request("GET", "/auth/me")
            return resp.json() if resp.status_code == 200 else None
        except httpx.RequestError:
            return None

    def list_clientes(self, q: str = "") -> list[dict]:
        try:
            params = {"q": q} if q else {}
            resp = self._request("GET", "/clientes", params=params)
            return resp.json() if resp.status_code == 200 else []
        except httpx.RequestError:
            return []

    def get_cliente(self, cliente_id: str) -> dict | None:
        try:
            resp = self._request("GET", f"/clientes/{cliente_id}")
            return resp.json() if resp.status_code == 200 else None
        except httpx.RequestError:
            return None

    def create_cliente(self, data: dict) -> dict | None:
        try:
            resp = self._request("POST", "/clientes", json=data)
            return resp.json() if resp.status_code == 201 else None
        except httpx.RequestError:
            return None

    def update_cliente(self, cliente_id: str, data: dict) -> dict | None:
        try:
            resp = self._request(
                "PUT", f"/clientes/{cliente_id}", json=data
            )
            return resp.json() if resp.status_code == 200 else None
        except httpx.RequestError:
            return None

    def delete_cliente(self, cliente_id: str) -> bool:
        try:
            resp = self._request("DELETE", f"/clientes/{cliente_id}")
            return resp.status_code == 200
        except httpx.RequestError:
            return False

    def list_vehiculos(self, q: str = "", id_cliente: str = "") -> list[dict]:
        try:
            params = {}
            if q:
                params["q"] = q
            if id_cliente:
                params["id_cliente"] = id_cliente
            resp = self._request("GET", "/vehiculos", params=params)
            return resp.json() if resp.status_code == 200 else []
        except httpx.RequestError:
            return []

    def get_vehiculo(self, vehiculo_id: str) -> dict | None:
        try:
            resp = self._request("GET", f"/vehiculos/{vehiculo_id}")
            return resp.json() if resp.status_code == 200 else None
        except httpx.RequestError:
            return None

    def create_vehiculo(self, data: dict) -> dict | None:
        try:
            resp = self._request("POST", "/vehiculos", json=data)
            return resp.json() if resp.status_code == 201 else None
        except httpx.RequestError:
            return None

    def update_vehiculo(self, vehiculo_id: str, data: dict) -> dict | None:
        try:
            resp = self._request(
                "PUT", f"/vehiculos/{vehiculo_id}", json=data
            )
            return resp.json() if resp.status_code == 200 else None
        except httpx.RequestError:
            return None

    def delete_vehiculo(self, vehiculo_id: str) -> bool:
        try:
            resp = self._request("DELETE", f"/vehiculos/{vehiculo_id}")
            return resp.status_code == 200
        except httpx.RequestError:
            return False

    def buscar_matricula(self, q: str) -> list[dict]:
        try:
            resp = self._request(
                "GET", "/vehiculos/buscar-matricula", params={"q": q}
            )
            return resp.json() if resp.status_code == 200 else []
        except httpx.RequestError:
            return []

    def list_ordenes(
        self,
        estado: str = "",
        id_mecanico: str = "",
        id_vehiculo: str = "",
    ) -> list[dict]:
        try:
            params = {}
            if estado:
                params["estado"] = estado
            if id_mecanico:
                params["id_mecanico"] = id_mecanico
            if id_vehiculo:
                params["id_vehiculo"] = id_vehiculo
            resp = self._request("GET", "/ordenes", params=params)
            return resp.json() if resp.status_code == 200 else []
        except httpx.RequestError:
            return []

    def get_orden(self, orden_id: str) -> dict | None:
        try:
            resp = self._request("GET", f"/ordenes/{orden_id}")
            return resp.json() if resp.status_code == 200 else None
        except httpx.RequestError:
            return None

    def create_orden(self, data: dict) -> dict | None:
        try:
            resp = self._request("POST", "/ordenes", json=data)
            return resp.json() if resp.status_code == 201 else None
        except httpx.RequestError:
            return None

    def update_orden(self, orden_id: str, data: dict) -> dict | None:
        try:
            resp = self._request("PUT", f"/ordenes/{orden_id}", json=data)
            return resp.json() if resp.status_code == 200 else None
        except httpx.RequestError:
            return None

    def cambiar_estado_orden(self, orden_id: str, estado: str) -> dict | None:
        try:
            resp = self._request(
                "PATCH",
                f"/ordenes/{orden_id}/estado",
                json={"estado": estado},
            )
            return resp.json() if resp.status_code == 200 else None
        except httpx.RequestError:
            return None

    def add_detalle(self, orden_id: str, data: dict) -> dict | None:
        try:
            resp = self._request(
                "POST", f"/ordenes/{orden_id}/detalles", json=data
            )
            return resp.json() if resp.status_code == 201 else None
        except httpx.RequestError:
            return None

    def update_detalle(self, detalle_id: str, data: dict) -> dict | None:
        try:
            resp = self._request(
                "PATCH", f"/ordenes/detalles/{detalle_id}", json=data
            )
            return resp.json() if resp.status_code == 200 else None
        except httpx.RequestError:
            return None

    def delete_detalle(self, detalle_id: str) -> bool:
        try:
            resp = self._request(
                "DELETE", f"/ordenes/detalles/{detalle_id}"
            )
            return resp.status_code == 200
        except httpx.RequestError:
            return False

    def list_articulos(
        self, q: str = "", stock_bajo: bool = False
    ) -> list[dict]:
        try:
            params = {}
            if q:
                params["q"] = q
            if stock_bajo:
                params["stock_bajo"] = "true"
            resp = self._request("GET", "/articulos", params=params)
            return resp.json() if resp.status_code == 200 else []
        except httpx.RequestError:
            return []

    def get_articulo(self, articulo_id: str) -> dict | None:
        try:
            resp = self._request("GET", f"/articulos/{articulo_id}")
            return resp.json() if resp.status_code == 200 else None
        except httpx.RequestError:
            return None

    def create_articulo(self, data: dict) -> dict | None:
        try:
            resp = self._request("POST", "/articulos", json=data)
            return resp.json() if resp.status_code == 201 else None
        except httpx.RequestError:
            return None

    def update_articulo(self, articulo_id: str, data: dict) -> dict | None:
        try:
            resp = self._request(
                "PUT", f"/articulos/{articulo_id}", json=data
            )
            return resp.json() if resp.status_code == 200 else None
        except httpx.RequestError:
            return None

    def delete_articulo(self, articulo_id: str) -> bool:
        try:
            resp = self._request("DELETE", f"/articulos/{articulo_id}")
            return resp.status_code == 200
        except httpx.RequestError:
            return False

    def ajustar_stock(self, articulo_id: str, cantidad: int) -> dict | None:
        try:
            resp = self._request(
                "PATCH",
                f"/articulos/{articulo_id}/stock",
                json={"cantidad": cantidad},
            )
            return resp.json() if resp.status_code == 200 else None
        except httpx.RequestError:
            return None

    def list_citas(
        self,
        fecha_desde: str = "",
        fecha_hasta: str = "",
        estado: str = "",
    ) -> list[dict]:
        try:
            params = {}
            if fecha_desde:
                params["fecha_desde"] = fecha_desde
            if fecha_hasta:
                params["fecha_hasta"] = fecha_hasta
            if estado:
                params["estado"] = estado
            resp = self._request("GET", "/citas", params=params)
            return resp.json() if resp.status_code == 200 else []
        except httpx.RequestError:
            return []

    def create_cita(self, data: dict) -> dict | None:
        try:
            resp = self._request("POST", "/citas", json=data)
            return resp.json() if resp.status_code == 201 else None
        except httpx.RequestError:
            return None

    def update_cita(self, cita_id: str, data: dict) -> dict | None:
        try:
            resp = self._request("PUT", f"/citas/{cita_id}", json=data)
            return resp.json() if resp.status_code == 200 else None
        except httpx.RequestError:
            return None

    def get_cita(self, cita_id: str) -> dict | None:
        try:
            resp = self._request("GET", f"/citas/{cita_id}")
            return resp.json() if resp.status_code == 200 else None
        except httpx.RequestError:
            return None

    def delete_cita(self, cita_id: str) -> bool:
        try:
            resp = self._request("DELETE", f"/citas/{cita_id}")
            return resp.status_code == 200
        except httpx.RequestError:
            return False

    def list_facturas(self, estado: str = "") -> list[dict]:
        try:
            params = {}
            if estado:
                params["estado"] = estado
            resp = self._request("GET", "/facturas", params=params)
            return resp.json() if resp.status_code == 200 else []
        except httpx.RequestError:
            return []

    def get_factura(self, factura_id: str) -> dict | None:
        try:
            resp = self._request("GET", f"/facturas/{factura_id}")
            return resp.json() if resp.status_code == 200 else None
        except httpx.RequestError:
            return None

    def create_factura(self, data: dict) -> dict | None:
        try:
            resp = self._request("POST", "/facturas", json=data)
            return resp.json() if resp.status_code == 201 else None
        except httpx.RequestError:
            return None

    def update_factura(self, factura_id: str, data: dict) -> dict | None:
        try:
            resp = self._request(
                "PATCH", f"/facturas/{factura_id}", json=data
            )
            return resp.json() if resp.status_code == 200 else None
        except httpx.RequestError:
            return None

    def dashboard_resumen(self) -> dict | None:
        try:
            resp = self._request("GET", "/dashboard/resumen")
            return resp.json() if resp.status_code == 200 else None
        except httpx.RequestError:
            return None

    def dashboard_ordenes_activas(self) -> list[dict]:
        try:
            resp = self._request("GET", "/dashboard/ordenes-activas")
            return resp.json() if resp.status_code == 200 else []
        except httpx.RequestError:
            return []

    def vehiculos_cliente(self, cliente_id: str) -> list[dict]:
        try:
            resp = self._request(
                "GET", f"/clientes/{cliente_id}/vehiculos"
            )
            return resp.json() if resp.status_code == 200 else []
        except httpx.RequestError:
            return []


API = ApiClient()
