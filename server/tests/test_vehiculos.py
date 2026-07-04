from fastapi.testclient import TestClient


def test_crear_vehiculo(client: TestClient, auth_headers: dict):
    response = client.post(
        "/api/v1/vehiculos",
        json={
            "marca": "Honda",
            "modelo": "CBR 600",
            "matricula": "1234ABC",
            "anio": 2020,
            "tipo_vehiculo": "MOTO",
        },
        headers=auth_headers,
    )
    assert response.status_code == 201
    data = response.json()
    assert data["marca"] == "Honda"
    assert data["modelo"] == "CBR 600"
    assert "id" in data


def test_buscar_matricula(client: TestClient, auth_headers: dict):
    client.post(
        "/api/v1/vehiculos",
        json={
            "marca": "Yamaha",
            "modelo": "MT-07",
            "matricula": "9999XYZ",
        },
        headers=auth_headers,
    )

    response = client.get(
        "/api/v1/vehiculos/buscar-matricula?q=9999",
        headers=auth_headers,
    )
    assert response.status_code == 200
    data = response.json()
    assert len(data) >= 1
    assert data[0]["matricula"] == "9999XYZ"


def test_listar_vehiculos(client: TestClient, auth_headers: dict):
    client.post(
        "/api/v1/vehiculos",
        json={"marca": "Kawasaki", "modelo": "Ninja"},
        headers=auth_headers,
    )

    response = client.get("/api/v1/vehiculos", headers=auth_headers)
    assert response.status_code == 200
    data = response.json()
    assert len(data) >= 1


def test_obtener_vehiculo(client: TestClient, auth_headers: dict):
    created = client.post(
        "/api/v1/vehiculos",
        json={"marca": "Suzuki", "modelo": "GSX-R"},
        headers=auth_headers,
    ).json()

    response = client.get(
        f"/api/v1/vehiculos/{created['id']}", headers=auth_headers
    )
    assert response.status_code == 200
    assert response.json()["modelo"] == "GSX-R"


def test_actualizar_vehiculo(client: TestClient, auth_headers: dict):
    created = client.post(
        "/api/v1/vehiculos",
        json={"marca": "Ducati", "modelo": "Panigale V2"},
        headers=auth_headers,
    ).json()

    response = client.put(
        f"/api/v1/vehiculos/{created['id']}",
        json={"kilometros": 15000},
        headers=auth_headers,
    )
    assert response.status_code == 200
    assert response.json()["kilometros"] == 15000
