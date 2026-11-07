import pytest
from pydantic import ValidationError
from sqlalchemy import select
from starlette.websockets import WebSocketDisconnect

from app.config import DEFAULT_SECRET_KEY, Settings
from app.models.audit_log import AuditLog
from app.models.usuario import Usuario
from tests.conftest import TEST_PASSWORD


def _register(client, email: str, password: str = TEST_PASSWORD):
    return client.post(
        "/api/v1/auth/registro-taller",
        json={
            "nombre_taller": "Taller Seguridad",
            "nombre_admin": "Admin Seguridad",
            "email_admin": email,
            "password": password,
        },
    )


def test_security_headers_and_request_id(client):
    response = client.get("/healthz", headers={"X-Request-ID": "req-seguro-123"})

    assert response.status_code == 200
    assert response.headers["X-Request-ID"] == "req-seguro-123"
    assert response.headers["X-Content-Type-Options"] == "nosniff"
    assert response.headers["X-Frame-Options"] == "DENY"
    assert response.headers["Referrer-Policy"] == "no-referrer"
    assert "default-src 'none'" in response.headers["Content-Security-Policy"]


def test_auth_errors_are_no_store_and_challenge(client):
    response = client.get("/api/v1/auth/me")

    assert response.status_code == 401
    assert response.headers["WWW-Authenticate"] == "Bearer"
    assert response.headers["Cache-Control"] == "no-store"
    assert response.headers["X-Request-ID"]


def test_password_policy_rejects_weak_values(client):
    short = _register(client, "short@test.com", "Ab1")
    no_digit = _register(client, "nodigit@test.com", "solamenteletras")
    common = _register(client, "common@test.com", "password")

    assert short.status_code == 422
    assert no_digit.status_code == 422
    assert common.status_code == 422


def test_login_rate_limit_returns_429(client):
    assert _register(client, "ratelimit@test.com").status_code == 200

    for _ in range(10):
        response = client.post(
            "/api/v1/auth/login",
            json={"email": "ratelimit@test.com", "password": "Incorrecta123"},
        )
        assert response.status_code == 401

    blocked = client.post(
        "/api/v1/auth/login",
        json={"email": "ratelimit@test.com", "password": "Incorrecta123"},
    )
    assert blocked.status_code == 429
    assert int(blocked.headers["Retry-After"]) >= 1


def test_account_locks_after_repeated_failures(client, db_session):
    assert _register(client, "lockout@test.com").status_code == 200

    for _ in range(5):
        response = client.post(
            "/api/v1/auth/login",
            json={"email": "lockout@test.com", "password": "Incorrecta123"},
        )
        assert response.status_code == 401

    locked = client.post(
        "/api/v1/auth/login",
        json={"email": "lockout@test.com", "password": TEST_PASSWORD},
    )
    assert locked.status_code == 401

    user = db_session.query(Usuario).filter(Usuario.email == "lockout@test.com").one()
    assert user.failed_login_attempts >= 5
    assert user.locked_until is not None


def test_token_version_revokes_old_sessions(client, auth_headers, db_session):
    user = db_session.query(Usuario).filter(Usuario.email == "admin@test.com").one()
    user.token_version = int(user.token_version or 0) + 1
    db_session.commit()

    response = client.get("/api/v1/auth/me", headers=auth_headers)

    assert response.status_code == 401
    assert "revocada" in response.json()["detail"].lower()


def test_pin_is_hashed_and_duplicates_are_rejected(
    client, auth_headers, other_auth_headers, db_session
):
    change = client.patch(
        "/api/v1/auth/me/pin",
        json={"pin_code": "246810"},
        headers=auth_headers,
    )
    assert change.status_code == 200

    user = db_session.query(Usuario).filter(Usuario.email == "admin@test.com").one()
    assert user.pin_code.startswith("$2")
    assert user.pin_lookup

    login = client.post("/api/v1/auth/login-pin", json={"pin_code": "246810"})
    assert login.status_code == 200

    duplicate = client.patch(
        "/api/v1/auth/me/pin",
        json={"pin_code": "246810"},
        headers=other_auth_headers,
    )
    assert duplicate.status_code == 409


def test_trivial_pin_is_rejected(client, auth_headers):
    response = client.patch(
        "/api/v1/auth/me/pin",
        json={"pin_code": "111111"},
        headers=auth_headers,
    )
    assert response.status_code == 422


def test_legacy_plaintext_pin_is_upgraded(client, auth_headers, db_session):
    user = db_session.query(Usuario).filter(Usuario.email == "admin@test.com").one()
    user.pin_code = "4321"
    user.pin_lookup = None
    db_session.commit()

    response = client.post("/api/v1/auth/login-pin", json={"pin_code": "4321"})

    assert response.status_code == 200
    db_session.expire_all()
    upgraded = db_session.query(Usuario).filter(Usuario.id == user.id).one()
    assert upgraded.pin_code.startswith("$2")
    assert upgraded.pin_lookup


def test_legacy_plaintext_pin_respects_requested_taller(
    client, auth_headers, other_auth_headers, db_session
):
    user = db_session.query(Usuario).filter(Usuario.email == "admin@test.com").one()
    user.pin_code = "4321"
    user.pin_lookup = None
    db_session.commit()

    # A PIN that belongs to another workshop must not authenticate.
    denied = client.post(
        "/api/v1/auth/login-pin",
        json={
            "pin_code": "4321",
            "id_taller": other_auth_headers["X-Taller-ID"],
        },
    )
    assert denied.status_code == 401
    db_session.expire_all()
    untouched = db_session.query(Usuario).filter(Usuario.id == user.id).one()
    assert untouched.pin_lookup is None

    allowed = client.post(
        "/api/v1/auth/login-pin",
        json={"pin_code": "4321", "id_taller": auth_headers["X-Taller-ID"]},
    )
    assert allowed.status_code == 200


def test_websocket_requires_a_valid_tenant_token(client, auth_headers):
    taller_id = auth_headers["X-Taller-ID"]
    token = auth_headers["Authorization"].split(" ", 1)[1]

    with pytest.raises(WebSocketDisconnect) as denied:
        with client.websocket_connect(f"/ws/taller/{taller_id}") as websocket:
            websocket.receive_text()
    assert denied.value.code == 1008

    with client.websocket_connect(f"/ws/taller/{taller_id}?token={token}") as websocket:
        websocket.send_text("ping")


def test_websocket_rejects_other_tenant(client, auth_headers, other_auth_headers):
    taller_id = auth_headers["X-Taller-ID"]
    other_token = other_auth_headers["Authorization"].split(" ", 1)[1]

    with pytest.raises(WebSocketDisconnect) as denied:
        with client.websocket_connect(
            f"/ws/taller/{taller_id}?token={other_token}"
        ) as websocket:
            websocket.receive_text()
    assert denied.value.code == 1008


def test_rbac_and_audit_trail(client, auth_headers, mecanico_headers):
    created = client.post(
        "/api/v1/clientes",
        json={"nombre": "Cliente auditado"},
        headers=auth_headers,
    )
    assert created.status_code == 201

    denied = client.delete(
        f"/api/v1/clientes/{created.json()['id']}",
        headers=mecanico_headers,
    )
    assert denied.status_code == 403

    forbidden_audit = client.get("/api/v1/auditoria/eventos", headers=mecanico_headers)
    assert forbidden_audit.status_code == 403

    events = client.get("/api/v1/auditoria/eventos", headers=auth_headers)
    assert events.status_code == 200
    actions = {(event["accion"], event["entidad"]) for event in events.json()}
    assert ("data.create", "clientes") in actions
    assert ("authz.denied", "endpoint") in actions


def test_password_change_revokes_old_token(client, auth_headers):
    changed = client.post(
        "/api/v1/auth/me/password",
        json={"current_password": TEST_PASSWORD, "new_password": "NuevaClave123"},
        headers=auth_headers,
    )
    assert changed.status_code == 200
    new_token = changed.json()["access_token"]

    old_session = client.get("/api/v1/auth/me", headers=auth_headers)
    new_session = client.get(
        "/api/v1/auth/me",
        headers={"Authorization": f"Bearer {new_token}"},
    )

    assert old_session.status_code == 401
    assert new_session.status_code == 200


def test_production_config_rejects_insecure_values():
    with pytest.raises(ValidationError):
        Settings(
            _env_file=None,
            environment="production",
            secret_key=DEFAULT_SECRET_KEY,
            debug=False,
            allowed_origins="https://app.example.com",
            trusted_hosts="api.example.com",
            force_https=True,
        )

    with pytest.raises(ValidationError):
        Settings(
            _env_file=None,
            environment="production",
            secret_key="x" * 48,
            debug=True,
            allowed_origins="https://app.example.com",
            trusted_hosts="api.example.com",
            force_https=True,
        )

    valid = Settings(
        _env_file=None,
        environment="production",
        secret_key="x" * 48,
        debug=False,
        allowed_origins="https://app.example.com",
        trusted_hosts="api.example.com",
        force_https=True,
    )
    assert valid.force_https is True


def test_audit_log_is_tenant_scoped(client, auth_headers, other_auth_headers):
    client.post(
        "/api/v1/clientes",
        json={"nombre": "Cliente tenant uno"},
        headers=auth_headers,
    )
    client.post(
        "/api/v1/clientes",
        json={"nombre": "Cliente tenant dos"},
        headers=other_auth_headers,
    )

    response = client.get("/api/v1/auditoria/eventos", headers=other_auth_headers)
    assert response.status_code == 200
    event_text = str(response.json())
    assert "Cliente tenant dos" not in event_text
    assert all(
        event["id_taller"] == other_auth_headers["X-Taller-ID"]
        for event in response.json()
        if event["id_taller"]
    )


def test_audit_store_never_contains_secrets(client, db_session, auth_headers):
    assert _register(client, "audit-secret@test.com").status_code == 200
    client.post(
        "/api/v1/auth/login",
        json={"email": "audit-secret@test.com", "password": "Incorrecta123"},
    )

    rows = db_session.execute(select(AuditLog)).scalars().all()
    serialized = " ".join(f"{row.detalle or ''} {row.entidad_id or ''}" for row in rows)
    assert TEST_PASSWORD not in serialized
    assert "Incorrecta123" not in serialized


def test_audit_rows_cannot_be_updated_or_deleted(db_session):
    row = AuditLog(accion="audit.integrity", resultado="SUCCESS")
    db_session.add(row)
    db_session.commit()

    row.accion = "audit.tampered"
    with pytest.raises(ValueError, match="append-only"):
        db_session.commit()
    db_session.rollback()

    persisted = db_session.query(AuditLog).filter(AuditLog.id == row.id).one()
    db_session.delete(persisted)
    with pytest.raises(ValueError, match="append-only"):
        db_session.commit()
    db_session.rollback()
