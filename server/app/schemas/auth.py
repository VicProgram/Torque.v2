import uuid

from pydantic import BaseModel, EmailStr, Field


class LoginRequest(BaseModel):
    email: EmailStr
    password: str


class PinLoginRequest(BaseModel):
    pin_code: str


class SetPinRequest(BaseModel):
    pin_code: str = Field(min_length=4, max_length=6)


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    id_usuario: str
    id_taller: str
    rol: str
    nombre: str


class RegistroTallerRequest(BaseModel):
    nombre_taller: str
    nombre_admin: str
    email_admin: EmailStr
    password: str


class UsuarioResponse(BaseModel):
    id: uuid.UUID
    nombre: str
    email: str
    rol: str
    activo: bool
