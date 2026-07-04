from fastapi.testclient import TestClient


def test_registro_taller(client: TestClient):
    response = client.post(
        "/api/v1/auth/registro-taller",
        json={
            "nombre_taller": "Nuevo Taller",
            "nombre_admin": "Admin",
            "email_admin": "admin@nuevo.com",
            "password": "securepass",
        },
    )
    assert response.status_code == 200
    data = response.json()
    assert data["token_type"] == "bearer"
    assert data["rol"] == "ADMIN"
    assert "access_token" in data


def test_login(client: TestClient):
    # First register
    client.post(
        "/api/v1/auth/registro-taller",
        json={
            "nombre_taller": "Login Taller",
            "nombre_admin": "Admin Login",
            "email_admin": "login@test.com",
            "password": "test123",
        },
    )

    response = client.post(
        "/api/v1/auth/login",
        json={"email": "login@test.com", "password": "test123"},
    )
    assert response.status_code == 200
    data = response.json()
    assert data["token_type"] == "bearer"
    assert "access_token" in data


def test_login_wrong_password(client: TestClient):
    response = client.post(
        "/api/v1/auth/login",
        json={"email": "noexiste@test.com", "password": "wrong"},
    )
    assert response.status_code == 401


def test_me_without_token(client: TestClient):
    response = client.get("/api/v1/auth/me")
    assert response.status_code == 401


def test_me_with_token(client: TestClient, auth_headers: dict):
    response = client.get("/api/v1/auth/me", headers=auth_headers)
    assert response.status_code == 200
    data = response.json()
    assert data["nombre"] == "Admin Test"
    assert data["rol"] == "ADMIN"
