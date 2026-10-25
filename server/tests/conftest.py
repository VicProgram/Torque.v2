from collections.abc import Generator

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from app.core.security import create_access_token, hash_password
from app.database import Base, get_db
from app.main import app

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


@pytest.fixture
def auth_headers(client, db_session):
    """Create a test taller + admin and return auth headers."""
    from app.models.taller import Taller
    from app.models.usuario import Usuario

    taller = Taller(nombre="Test Taller")
    db_session.add(taller)
    db_session.flush()

    admin = Usuario(
        id_taller=taller.id,
        nombre="Admin Test",
        email="admin@test.com",
        password_hash=hash_password("test123"),
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
