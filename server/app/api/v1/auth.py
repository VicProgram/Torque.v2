from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from sqlalchemy import or_

from app.database import get_db
from app.models.taller import Taller
from app.models.usuario import Usuario
from app.schemas.auth import (
    LoginRequest,
    PinLoginRequest,
    SetPinRequest,
    TokenResponse,
    RegistroTallerRequest,
    UsuarioResponse,
)
from app.core.security import (
    hash_password,
    verify_password,
    create_access_token,
    create_pin_token,
)
from app.api.deps import get_current_user

router = APIRouter()


@router.post("/registro-taller", response_model=TokenResponse)
def registrar_taller(req: RegistroTallerRequest, db: Session = Depends(get_db)):
    existing = db.query(Usuario).filter(
        Usuario.email == req.email_admin
    ).first()
    if existing:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="El email ya está registrado",
        )

    taller = Taller(nombre=req.nombre_taller)
    db.add(taller)
    db.flush()

    admin = Usuario(
        id_taller=taller.id,
        nombre=req.nombre_admin,
        email=req.email_admin,
        password_hash=hash_password(req.password),
        rol="ADMIN",
    )
    db.add(admin)
    db.commit()
    db.refresh(admin)

    token = create_access_token(
        data={"sub": str(admin.id), "id_taller": str(taller.id), "rol": "ADMIN"}
    )
    return TokenResponse(
        access_token=token,
        id_usuario=str(admin.id),
        id_taller=str(taller.id),
        rol="ADMIN",
        nombre=admin.nombre,
    )


@router.post("/login", response_model=TokenResponse)
def login(req: LoginRequest, db: Session = Depends(get_db)):
    user = db.query(Usuario).filter(
        or_(Usuario.email == req.email, Usuario.nombre == req.email)
    ).first()
    if not user or not verify_password(req.password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Credenciales inválidas",
        )
    if not user.activo:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Usuario inactivo",
        )

    token = create_access_token(
        data={
            "sub": str(user.id),
            "id_taller": str(user.id_taller),
            "rol": user.rol,
        }
    )
    return TokenResponse(
        access_token=token,
        id_usuario=str(user.id),
        id_taller=str(user.id_taller),
        rol=user.rol,
        nombre=user.nombre,
    )


@router.post("/login-pin", response_model=TokenResponse)
def login_pin(req: PinLoginRequest, db: Session = Depends(get_db)):
    user = db.query(Usuario).filter(
        Usuario.pin_code == req.pin_code, Usuario.activo == True
    ).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="PIN inválido",
        )

    token = create_pin_token(
        id_usuario=str(user.id),
        id_taller=str(user.id_taller),
    )
    return TokenResponse(
        access_token=token,
        id_usuario=str(user.id),
        id_taller=str(user.id_taller),
        rol=user.rol,
        nombre=user.nombre,
    )


@router.get("/me", response_model=UsuarioResponse)
def me(user: Usuario = Depends(get_current_user)):
    return UsuarioResponse(
        id=str(user.id),
        nombre=user.nombre,
        email=user.email,
        rol=user.rol,
        activo=user.activo,
    )


@router.patch("/me/pin")
def set_pin(
    req: SetPinRequest,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    user.pin_code = req.pin_code
    db.commit()
    return {"mensaje": "PIN actualizado"}
