from collections.abc import Generator

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from app import database
from app.core.rate_limit import limiter
from app.core.security import create_access_token, hash_password
from app.database import Base, get_db
from app.main import app

TEST_PASSWORD = "Seguro1234"
SQLALCHEMY_DATABASE_URL = "sqlite:///./test_torque.db"
engine = create_engine(
    SQLALCHEMY_DATABASE_URL,
    connect_args={"check_same_thread": False},
    poolclass=StaticPool,
)
TestingSessionLocal = sessionmaker(
    autocommit=False, autoflush=False, bind=engine
)


def override_get_db():
    db = TestingSessionLocal()
    try:
        yield db
    finally:
        db.close()


app.dependency_overrides[get_db] = override_get_db
# The WebSocket authenticator opens its own session; point it at the test DB.
database.SessionLocal = TestingSessionLocal


@pytest.fixture(autouse=True)
def reset_rate_limiter():
    limiter.reset()
    yield
    limiter.reset()


@pytest.fixture
def db_session():
    Base.metadata.create_all(bind=engine)
    session = TestingSessionLocal()
    try:
        yield session
    finally:
        session.close()
        Base.metadata.drop_all(bind=engine)


@pytest.fixture
def client() -> Generator:
    Base.metadata.create_all(bind=engine)
    with TestClient(app) as c:
        yield c
    Base.metadata.drop_all(bind=engine)


def _create_taller_admin(db_session, nombre: str, email: str, admin_nombre: str) -> dict:
    from app.models.taller import Taller
    from app.models.usuario import Usuario

    taller = Taller(nombre=nombre)
    db_session.add(taller)
    db_session.flush()

    admin = Usuario(
        id_taller=taller.id,
        nombre=admin_nombre,
        email=email,
        password_hash=hash_password(TEST_PASSWORD),
        rol="ADMIN",
    )
    db_session.add(admin)
    db_session.commit()

    token = create_access_token(
        data={
            "sub": str(admin.id),
            "id_taller": str(taller.id),
            "rol": "ADMIN",
        }
    )
    return {
        "Authorization": f"Bearer {token}",
        "X-Taller-ID": str(taller.id),
    }


@pytest.fixture
def auth_headers(client, db_session):
    """Create a test taller + admin and return auth headers."""
    return _create_taller_admin(db_session, "Test Taller", "admin@test.com", "Admin Test")


@pytest.fixture
def other_auth_headers(client, db_session):
    """Create a second taller + admin to check tenant isolation."""
    return _create_taller_admin(db_session, "Other Taller", "other@test.com", "Admin Other")


@pytest.fixture
def mecanico_headers(client, auth_headers, db_session):
    """Create a mechanic in the default test workshop."""
    from app.models.usuario import Usuario

    taller_id = auth_headers["X-Taller-ID"]
    mecanico = Usuario(
        id_taller=taller_id,
        nombre="Mecánico Test",
        email="mecanico@test.com",
        password_hash=hash_password(TEST_PASSWORD),
        rol="MECANICO",
    )
    db_session.add(mecanico)
    db_session.commit()
    db_session.refresh(mecanico)

    token = create_access_token(
        data={
            "sub": str(mecanico.id),
            "id_taller": str(mecanico.id_taller),
            "rol": mecanico.rol,
        }
    )
    return {
        "Authorization": f"Bearer {token}",
        "X-Taller-ID": str(mecanico.id_taller),
    }
