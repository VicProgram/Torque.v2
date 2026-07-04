from fastapi.testclient import TestClient


def test_crear_cliente(client: TestClient, auth_headers: dict):
    response = client.post(
        "/api/v1/clientes",
        json={"nombre": "Juan Pérez", "telefono": "600123456"},
        headers=auth_headers,
    )
    assert response.status_code == 201
    data = response.json()
    assert data["nombre"] == "Juan Pérez"
    assert data["telefono"] == "600123456"
    assert "id" in data


def test_listar_clientes(client: TestClient, auth_headers: dict):
    client.post(
        "/api/v1/clientes",
        json={"nombre": "Cliente A"},
        headers=auth_headers,
    )
    client.post(
        "/api/v1/clientes",
        json={"nombre": "Cliente B"},
        headers=auth_headers,
    )

    response = client.get("/api/v1/clientes", headers=auth_headers)
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 2


def test_obtener_cliente(client: TestClient, auth_headers: dict):
    created = client.post(
        "/api/v1/clientes",
        json={"nombre": "Cliente X"},
        headers=auth_headers,
    ).json()

    response = client.get(
        f"/api/v1/clientes/{created['id']}", headers=auth_headers
    )
    assert response.status_code == 200
    assert response.json()["nombre"] == "Cliente X"


def test_actualizar_cliente(client: TestClient, auth_headers: dict):
    created = client.post(
        "/api/v1/clientes",
        json={"nombre": "Viejo nombre"},
        headers=auth_headers,
    ).json()

    response = client.put(
        f"/api/v1/clientes/{created['id']}",
        json={"nombre": "Nuevo nombre"},
        headers=auth_headers,
    )
    assert response.status_code == 200
    assert response.json()["nombre"] == "Nuevo nombre"


def test_eliminar_cliente(client: TestClient, auth_headers: dict):
    created = client.post(
        "/api/v1/clientes",
        json={"nombre": "A eliminar"},
        headers=auth_headers,
    ).json()

    response = client.delete(
        f"/api/v1/clientes/{created['id']}", headers=auth_headers
    )
    assert response.status_code == 200

    response = client.get(
        f"/api/v1/clientes/{created['id']}", headers=auth_headers
    )
    assert response.status_code == 404


def test_cliente_no_pertenece_al_taller(client: TestClient, auth_headers: dict):
    response = client.get(
        "/api/v1/clientes/00000000-0000-0000-0000-000000000000",
        headers=auth_headers,
    )
    assert response.status_code == 404
