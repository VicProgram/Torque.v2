import hmac
from datetime import UTC, datetime, timedelta

from fastapi import APIRouter, Depends, HTTPException, Request, status
from sqlalchemy import or_
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.api.deps import get_current_user
from app.config import get_settings
from app.core.audit import actor_from_request, audit_event, set_audit_actor
from app.core.exceptions import ConflictException
from app.core.rate_limit import (
    rate_limit_login,
    rate_limit_pin,
    rate_limit_registration,
)
from app.core.security import (
    burn_password_check,
    create_access_token,
    create_pin_token,
    hash_password,
    hash_pin,
    pin_lookup,
    verify_password,
    verify_pin,
)
from app.database import get_db
from app.models.taller import Taller
from app.models.usuario import Usuario
from app.schemas.auth import (
    ChangePasswordRequest,
    LoginRequest,
    PinLoginRequest,
    RegistroTallerRequest,
    SetPinRequest,
    TokenResponse,
    UsuarioResponse,
)
from app.schemas.common import Mensaje

settings = get_settings()
router = APIRouter()


def _token_response(user: Usuario, token: str) -> TokenResponse:
    return TokenResponse(
        access_token=token,
        id_usuario=str(user.id),
        id_taller=str(user.id_taller),
        rol=user.rol,
        nombre=user.nombre,
    )


def _access_token_for(user: Usuario) -> str:
    return create_access_token(
        data={
            "sub": str(user.id),
            "id_taller": str(user.id_taller),
            "rol": user.rol,
        },
        token_version=int(user.token_version or 0),
    )


def _pin_token_for(user: Usuario) -> str:
    return create_pin_token(
        id_usuario=str(user.id),
        id_taller=str(user.id_taller),
        token_version=int(user.token_version or 0),
    )


def _locked_until(user: Usuario) -> datetime | None:
    if user.locked_until is None:
        return None
    locked = user.locked_until
    if locked.tzinfo is None:
        locked = locked.replace(tzinfo=UTC)
    return locked if locked > datetime.now(UTC) else None


def _record_failed_attempt(user: Usuario) -> None:
    user.failed_login_attempts = int(user.failed_login_attempts or 0) + 1
    if user.failed_login_attempts >= settings.login_lockout_threshold:
        user.locked_until = datetime.now(UTC) + timedelta(
            minutes=settings.login_lockout_minutes
        )


def _record_success(db: Session, user: Usuario, request: Request, action: str) -> None:
    user.failed_login_attempts = 0
    user.locked_until = None
    user.ultimo_acceso = datetime.now(UTC)
    actor = actor_from_request(request, user=user)
    set_audit_actor(actor)
    db.info["audit_actor"] = actor
    audit_event(db, action, outcome="SUCCESS", entity="usuario", entity_id=str(user.id))


@router.post("/registro-taller", response_model=TokenResponse)
def registrar_taller(
    req: RegistroTallerRequest,
    request: Request,
    db: Session = Depends(get_db),
):
    rate_limit_registration(request)

    existing = db.query(Usuario).filter(
        Usuario.email == req.email_admin
    ).first()
    if existing:
        audit_event(
            db,
            "auth.register",
            outcome="FAILURE",
            email=str(req.email_admin),
            detail="email_already_registered",
        )
        db.commit()
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
        password_changed_at=datetime.now(UTC),
        rol="ADMIN",
    )
    db.add(admin)
    try:
        db.commit()
    except IntegrityError:
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="El email ya está registrado",
        ) from None
    db.refresh(admin)

    audit_event(
        db,
        "auth.register",
        outcome="SUCCESS",
        entity="taller",
        entity_id=str(taller.id),
        taller_id=str(taller.id),
        usuario_id=str(admin.id),
        email=str(admin.email),
    )
    db.commit()

    return _token_response(admin, _access_token_for(admin))


@router.post("/login", response_model=TokenResponse)
def login(
    req: LoginRequest,
    request: Request,
    db: Session = Depends(get_db),
):
    rate_limit_login(request)

    user = db.query(Usuario).filter(
        or_(Usuario.email == req.email, Usuario.nombre == req.email)
    ).first()
    if user is None:
        burn_password_check()
        audit_event(
            db,
            "auth.login",
            outcome="FAILURE",
            email=str(req.email),
            detail="invalid_credentials",
        )
        db.commit()
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Credenciales inválidas",
            headers={"WWW-Authenticate": "Bearer"},
        )

    if _locked_until(user) is not None:
        # Keep the response and timing indistinguishable from bad credentials so
        # an attacker cannot confirm that the account exists.
        burn_password_check()
        audit_event(
            db,
            "auth.login",
            outcome="DENIED",
            email=str(req.email),
            taller_id=user.id_taller,
            usuario_id=user.id,
            detail="account_locked",
        )
        db.commit()
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Credenciales inválidas",
            headers={"WWW-Authenticate": "Bearer"},
        )

    if not verify_password(req.password, user.password_hash):
        _record_failed_attempt(user)
        audit_event(
            db,
            "auth.login",
            outcome="FAILURE",
            email=str(user.email),
            taller_id=user.id_taller,
            usuario_id=user.id,
            detail="invalid_credentials",
        )
        db.commit()
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Credenciales inválidas",
            headers={"WWW-Authenticate": "Bearer"},
        )

    if not user.activo:
        audit_event(
            db,
            "auth.login",
            outcome="DENIED",
            email=str(user.email),
            taller_id=user.id_taller,
            usuario_id=user.id,
            detail="inactive_user",
        )
        db.commit()
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Credenciales inválidas",
            headers={"WWW-Authenticate": "Bearer"},
        )

    _record_success(db, user, request, "auth.login")
    db.commit()
    return _token_response(user, _access_token_for(user))


@router.post("/login-pin", response_model=TokenResponse)
def login_pin(
    req: PinLoginRequest,
    request: Request,
    db: Session = Depends(get_db),
):
    rate_limit_pin(request)
    lookup = pin_lookup(req.pin_code)

    query = db.query(Usuario).filter(
        Usuario.pin_lookup == lookup,
        Usuario.activo.is_(True),
    )
    if req.id_taller:
        query = query.filter(Usuario.id_taller == req.id_taller)
    user = query.first()

    # Compatibility path for workshops that still have plaintext PINs from
    # before the hardening migration. The value is upgraded on first use.
    if user is None:
        legacy_query = db.query(Usuario).filter(
            Usuario.pin_lookup.is_(None),
            Usuario.pin_code.isnot(None),
            Usuario.activo.is_(True),
        )
        if req.id_taller:
            legacy_query = legacy_query.filter(Usuario.id_taller == req.id_taller)
        legacy = legacy_query.all()
        for candidate in legacy:
            stored = candidate.pin_code or ""
            if stored and not stored.startswith("$2") and hmac.compare_digest(
                stored, req.pin_code
            ):
                user = candidate
                user.pin_code = hash_pin(req.pin_code)
                user.pin_lookup = lookup
                break

    if user is None:
        burn_password_check()
        audit_event(
            db,
            "auth.pin",
            outcome="FAILURE",
            detail="invalid_pin",
        )
        db.commit()
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="PIN inválido",
            headers={"WWW-Authenticate": "Bearer"},
        )

    if _locked_until(user) is not None:
        burn_password_check()
        audit_event(
            db,
            "auth.pin",
            outcome="DENIED",
            email=str(user.email),
            taller_id=user.id_taller,
            usuario_id=user.id,
            detail="account_locked",
        )
        db.commit()
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="PIN inválido",
            headers={"WWW-Authenticate": "Bearer"},
        )

    if not verify_pin(req.pin_code, user.pin_code or ""):
        _record_failed_attempt(user)
        audit_event(
            db,
            "auth.pin",
            outcome="FAILURE",
            email=str(user.email),
            taller_id=user.id_taller,
            usuario_id=user.id,
            detail="invalid_pin",
        )
        db.commit()
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="PIN inválido",
            headers={"WWW-Authenticate": "Bearer"},
        )

    _record_success(db, user, request, "auth.pin")
    try:
        db.commit()
    except IntegrityError:
        # Two legacy accounts cannot claim the same upgraded PIN.
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="PIN duplicado. Contacta con el administrador.",
        ) from None
    return _token_response(user, _pin_token_for(user))


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
    request: Request,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    lookup = pin_lookup(req.pin_code)
    duplicate = db.query(Usuario).filter(
        Usuario.pin_lookup == lookup,
        Usuario.id != user.id,
    ).first()
    if duplicate is not None:
        audit_event(
            db,
            "auth.pin.set",
            outcome="FAILURE",
            entity="usuario",
            entity_id=str(user.id),
            detail="pin_already_in_use",
        )
        db.commit()
        raise ConflictException("Ese PIN ya está en uso")

    user.pin_code = hash_pin(req.pin_code)
    user.pin_lookup = lookup
    # Revoke every previously issued token: a PIN change is a credential change.
    user.token_version = int(user.token_version or 0) + 1
    audit_event(
        db,
        "auth.pin.set",
        outcome="SUCCESS",
        entity="usuario",
        entity_id=str(user.id),
    )
    db.commit()
    return {
        "mensaje": "PIN actualizado. Vuelve a iniciar sesión en los demás dispositivos.",
    }


@router.post("/me/password", response_model=TokenResponse)
def change_password(
    req: ChangePasswordRequest,
    request: Request,
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if not verify_password(req.current_password, user.password_hash):
        audit_event(
            db,
            "auth.password.change",
            outcome="FAILURE",
            entity="usuario",
            entity_id=str(user.id),
            detail="wrong_current_password",
        )
        db.commit()
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="La contraseña actual no es correcta",
        )

    user.password_hash = hash_password(req.new_password)
    user.password_changed_at = datetime.now(UTC)
    user.token_version = int(user.token_version or 0) + 1
    audit_event(
        db,
        "auth.password.change",
        outcome="SUCCESS",
        entity="usuario",
        entity_id=str(user.id),
    )
    db.commit()
    return _token_response(user, _access_token_for(user))


@router.post("/logout", response_model=Mensaje)
def logout(
    user: Usuario = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    user.token_version = int(user.token_version or 0) + 1
    audit_event(
        db,
        "auth.logout",
        outcome="SUCCESS",
        entity="usuario",
        entity_id=str(user.id),
        detail="all_tokens_revoked",
    )
    db.commit()
    return Mensaje(detail="Sesión cerrada en todos los dispositivos")
