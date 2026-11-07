import re
import uuid

from pydantic import BaseModel, EmailStr, Field, field_validator

from app.config import get_settings

settings = get_settings()

_COMMON_PASSWORDS = {
    "12345678",
    "123456789",
    "1234567890",
    "password",
    "password1",
    "password123",
    "qwerty123",
    "letmein1",
    "admin123",
    "iloveyou",
    "welcome1",
    "changeme",
}
_TRIVIAL_PINS = {"000000", "111111", "123456", "654321", "121212", "112233"}


def _validate_password(value: str) -> str:
    if len(value) < settings.password_min_length:
        raise ValueError(
            f"La contraseña debe tener al menos {settings.password_min_length} caracteres"
        )
    if len(value) > settings.password_max_length:
        raise ValueError(
            f"La contraseña no puede superar {settings.password_max_length} caracteres"
        )
    if not re.search(r"[A-Za-z]", value) or not re.search(r"\d", value):
        raise ValueError("La contraseña debe incluir letras y números")
    if value.lower() in _COMMON_PASSWORDS:
        raise ValueError("La contraseña es demasiado común")
    return value


class LoginRequest(BaseModel):
    email: EmailStr
    password: str = Field(min_length=1, max_length=128)


class PinLoginRequest(BaseModel):
    # 4 digits are accepted only for legacy accounts; new PINs are 6 digits.
    pin_code: str = Field(min_length=4, max_length=6, pattern=r"^\d+$")
    id_taller: str | None = None


class SetPinRequest(BaseModel):
    pin_code: str = Field(min_length=6, max_length=6, pattern=r"^\d+$")

    @field_validator("pin_code")
    @classmethod
    def _strong_enough(cls, value: str) -> str:
        ascending = "0123456789"
        descending = "9876543210"
        sequences = {ascending[i : i + 6] for i in range(5)}
        sequences |= {descending[i : i + 6] for i in range(5)}
        if value in _TRIVIAL_PINS or len(set(value)) == 1:
            raise ValueError("El PIN es demasiado predecible")
        if value in sequences:
            raise ValueError("El PIN es demasiado predecible")
        return value


class ChangePasswordRequest(BaseModel):
    current_password: str = Field(min_length=1, max_length=128)
    new_password: str = Field(max_length=128)

    @field_validator("new_password")
    @classmethod
    def _strong_enough(cls, value: str) -> str:
        return _validate_password(value)


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    id_usuario: str
    id_taller: str
    rol: str
    nombre: str


class RegistroTallerRequest(BaseModel):
    nombre_taller: str = Field(min_length=1, max_length=200)
    nombre_admin: str = Field(min_length=1, max_length=100)
    email_admin: EmailStr
    password: str = Field(max_length=128)

    @field_validator("password")
    @classmethod
    def _strong_enough(cls, value: str) -> str:
        return _validate_password(value)


class UsuarioResponse(BaseModel):
    id: uuid.UUID
    nombre: str
    email: str
    rol: str
    activo: bool
