from sqlalchemy.orm import Session

from app.core.exceptions import ConflictException
from app.core.security import hash_password
from app.models.taller import Taller
from app.models.usuario import Usuario


def registrar_taller(
    db: Session,
    nombre_taller: str,
    nombre_admin: str,
    email_admin: str,
    password: str,
) -> tuple[Taller, Usuario]:
    existing = db.query(Usuario).filter(Usuario.email == email_admin).first()
    if existing:
        raise ConflictException("El email ya está registrado")

    taller = Taller(nombre=nombre_taller)
    db.add(taller)
    db.flush()

    admin = Usuario(
        id_taller=taller.id,
        nombre=nombre_admin,
        email=email_admin,
        password_hash=hash_password(password),
        rol="ADMIN",
    )
    db.add(admin)
    db.commit()
    db.refresh(taller)
    db.refresh(admin)

    return taller, admin
