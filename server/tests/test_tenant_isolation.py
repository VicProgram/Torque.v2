import pytest

from app.models.usuario import Usuario


@pytest.fixture
def other_data(client, other_auth_headers, db_session):
    """Create a client, vehicle, order and invoice owned by the second taller."""
    cliente = client.post(
        "/api/v1/clientes",
        json={"nombre": "Cliente ajeno"},
        headers=other_auth_headers,
    ).json()
    vehiculo = client.post(
        "/api/v1/vehiculos",
        json={"marca": "Yamaha", "modelo": "MT-07", "matricula": "9999ZZZ"},
        headers=other_auth_headers,
    ).json()
    orden = client.post(
        "/api/v1/ordenes",
        json={"id_vehiculo": vehiculo["id"]},
        headers=other_auth_headers,
    ).json()
    factura = client.post(
        "/api/v1/facturas",
        json={
            "id_orden_trabajo": orden["id"],
            "numero_factura": "AJENA-001",
            "fecha_emision": "2026-10-25",
            "base_imponible": 100,
            "total": 121,
        },
        headers=other_auth_headers,
    ).json()
    mecanico = db_session.query(Usuario).filter(Usuario.email == "other@test.com").one()
    return {
        "cliente": cliente,
        "vehiculo": vehiculo,
        "orden": orden,
        "factura": factura,
        "mecanico": mecanico,
    }


def _crear_vehiculo(client, headers, **extra):
    payload = {"marca": "Honda", "modelo": "CB500", "matricula": "1111AAA", **extra}
    return client.post("/api/v1/vehiculos", json=payload, headers=headers)


def _crear_orden(client, headers, vehiculo_id, **extra):
    return client.post(
        "/api/v1/ordenes",
        json={"id_vehiculo": vehiculo_id, **extra},
        headers=headers,
    )


def test_crear_vehiculo_con_cliente_ajeno_falla(client, auth_headers, other_data):
    response = _crear_vehiculo(
        client, auth_headers, id_cliente=other_data["cliente"]["id"]
    )
    assert response.status_code == 404


def test_actualizar_vehiculo_con_cliente_ajeno_falla(client, auth_headers, other_data):
    vehiculo = _crear_vehiculo(client, auth_headers).json()

    response = client.put(
        f"/api/v1/vehiculos/{vehiculo['id']}",
        json={"id_cliente": other_data["cliente"]["id"]},
        headers=auth_headers,
    )

    assert response.status_code == 404
    assert client.get(
        f"/api/v1/vehiculos/{vehiculo['id']}", headers=auth_headers
    ).json()["id_cliente"] is None


@pytest.mark.parametrize("campo", ["id_cliente", "id_vehiculo", "id_mecanico"])
def test_crear_cita_con_entidad_ajena_falla(
    client, auth_headers, other_data, campo
):
    valor = (
        other_data["mecanico"].id
        if campo == "id_mecanico"
        else other_data[campo.replace("id_", "")]["id"]
    )
    response = client.post(
        "/api/v1/citas",
        json={"fecha_hora": "2026-11-01T10:00:00", campo: str(valor)},
        headers=auth_headers,
    )
    assert response.status_code == 404


@pytest.mark.parametrize("campo", ["id_cliente", "id_vehiculo", "id_mecanico"])
def test_actualizar_cita_con_entidad_ajena_falla(
    client, auth_headers, other_data, campo
):
    cita = client.post(
        "/api/v1/citas",
        json={"fecha_hora": "2026-11-01T10:00:00"},
        headers=auth_headers,
    ).json()
    valor = (
        other_data["mecanico"].id
        if campo == "id_mecanico"
        else other_data[campo.replace("id_", "")]["id"]
    )

    response = client.put(
        f"/api/v1/citas/{cita['id']}", json={campo: str(valor)}, headers=auth_headers
    )
    assert response.status_code == 404


@pytest.mark.parametrize("campo", ["id_vehiculo", "id_cliente", "id_mecanico_asignado"])
def test_crear_orden_con_entidad_ajena_falla(
    client, auth_headers, other_data, campo
):
    propio = _crear_vehiculo(client, auth_headers).json()
    if campo == "id_vehiculo":
        payload = {"id_vehiculo": other_data["vehiculo"]["id"]}
    elif campo == "id_cliente":
        payload = {"id_vehiculo": propio["id"], "id_cliente": other_data["cliente"]["id"]}
    else:
        payload = {
            "id_vehiculo": propio["id"],
            "id_mecanico_asignado": str(other_data["mecanico"].id),
        }

    response = client.post("/api/v1/ordenes", json=payload, headers=auth_headers)
    assert response.status_code == 404


@pytest.mark.parametrize("campo", ["id_vehiculo", "id_cliente", "id_mecanico_asignado"])
def test_actualizar_orden_con_entidad_ajena_falla(
    client, auth_headers, other_data, campo
):
    propio = _crear_vehiculo(client, auth_headers).json()
    orden = _crear_orden(client, auth_headers, propio["id"]).json()
    if campo == "id_vehiculo":
        payload = {campo: other_data["vehiculo"]["id"]}
    elif campo == "id_cliente":
        payload = {campo: other_data["cliente"]["id"]}
    else:
        payload = {campo: str(other_data["mecanico"].id)}

    response = client.put(
        f"/api/v1/ordenes/{orden['id']}", json=payload, headers=auth_headers
    )
    assert response.status_code == 404


def test_crear_factura_con_orden_ajena_falla(client, auth_headers, other_data):
    response = client.post(
        "/api/v1/facturas",
        json={
            "id_orden_trabajo": other_data["orden"]["id"],
            "numero_factura": "PROPIA-001",
            "fecha_emision": "2026-10-25",
            "base_imponible": 100,
            "total": 121,
        },
        headers=auth_headers,
    )
    assert response.status_code == 404


def test_referencias_del_mismo_taller_siguen_funcionando(client, auth_headers):
    cliente = client.post(
        "/api/v1/clientes", json={"nombre": "Cliente propio"}, headers=auth_headers
    ).json()
    vehiculo = _crear_vehiculo(
        client, auth_headers, id_cliente=cliente["id"]
    ).json()

    cita = client.post(
        "/api/v1/citas",
        json={
            "fecha_hora": "2026-11-01T10:00:00",
            "id_cliente": cliente["id"],
            "id_vehiculo": vehiculo["id"],
        },
        headers=auth_headers,
    )
    orden = _crear_orden(
        client, auth_headers, vehiculo["id"], id_cliente=cliente["id"]
    )
    factura = client.post(
        "/api/v1/facturas",
        json={
            "id_orden_trabajo": orden.json()["id"],
            "numero_factura": "PROPIA-002",
            "fecha_emision": "2026-10-25",
            "base_imponible": 100,
            "total": 121,
        },
        headers=auth_headers,
    )

    assert cita.status_code == 201
    assert orden.status_code == 201
    assert factura.status_code == 201
    assert client.get("/api/v1/facturas", headers=auth_headers).status_code == 200
