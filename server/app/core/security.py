import hashlib
import hmac
import uuid
from datetime import UTC, datetime, timedelta

import bcrypt
import jwt
from jwt import PyJWTError

from app.config import get_settings

settings = get_settings()

# A valid bcrypt hash used to keep failed-login timing roughly constant when
# the supplied account does not exist. It never matches a real password.
# Public throwaway hash used only to equalise failed-login timing.
_DUMMY_PASSWORD_HASH = (  # nosec B105
    "$2b$12$KTkeuLvycv5e3qMssUAVgurgtYYnHbgv/ExoYriF9nFL1IbxivtNe"
)

_TOKEN_TYPES = {"access", "pin"}


def hash_password(password: str) -> str:
    return bcrypt.hashpw(
        password.encode("utf-8"), bcrypt.gensalt(rounds=12)
    ).decode("utf-8")


def verify_password(plain_password: str, hashed_password: str) -> bool:
    try:
        return bcrypt.checkpw(
            plain_password.encode("utf-8"), hashed_password.encode("utf-8")
        )
    except (ValueError, TypeError):
        return False


def burn_password_check() -> None:
    """Consume a bcrypt verification to reduce user-enumeration timing leaks."""
    bcrypt.checkpw(b"not-a-real-password", _DUMMY_PASSWORD_HASH.encode("utf-8"))


def hash_pin(pin: str) -> str:
    return hash_password(f"pin:{pin}")


def verify_pin(pin: str, hashed_pin: str) -> bool:
    return verify_password(f"pin:{pin}", hashed_pin)


def pin_lookup(pin: str) -> str:
    """Return a deterministic, secret-keyed lookup value for a PIN.

    The HMAC is only used to find the account without comparing bcrypt hashes
    for every user. Knowing the HMAC is not enough to log in: the bcrypt hash
    stored in ``pin_code`` is still verified.
    """
    digest = hmac.new(
        settings.pin_pepper_value.encode("utf-8"),
        pin.encode("utf-8"),
        hashlib.sha256,
    )
    return digest.hexdigest()


def create_access_token(
    data: dict,
    expires_delta: timedelta | None = None,
    # JWT type label, not a password.
    token_type: str = "access",  # nosec B107
    token_version: int = 0,
) -> str:
    now = datetime.now(UTC)
    expire = now + (
        expires_delta or timedelta(minutes=settings.access_token_expire_minutes)
    )
    to_encode = data.copy()
    to_encode.update(
        {
            "exp": expire,
            "iat": now,
            "nbf": now,
            "iss": settings.jwt_issuer,
            "aud": settings.jwt_audience,
            "jti": str(uuid.uuid4()),
            "type": token_type,
            "ver": token_version,
        }
    )
    return jwt.encode(to_encode, settings.secret_key, algorithm=settings.algorithm)


def decode_access_token(token: str, expected_type: str | None = None) -> dict | None:
    try:
        payload = jwt.decode(
            token,
            settings.secret_key,
            algorithms=[settings.algorithm],
            audience=settings.jwt_audience,
            issuer=settings.jwt_issuer,
            options={"require": ["exp", "iat", "sub"]},
        )
    except PyJWTError:
        return None

    token_type = payload.get("type")
    if token_type not in _TOKEN_TYPES:
        return None
    if expected_type is not None and token_type != expected_type:
        return None
    if not payload.get("sub"):
        return None
    return payload


def create_pin_token(
    id_usuario: str,
    id_taller: str,
    token_version: int = 0,
) -> str:
    return create_access_token(
        data={"sub": id_usuario, "id_taller": id_taller, "type": "pin"},
        expires_delta=timedelta(minutes=settings.pin_token_expire_minutes),
        # JWT type label, not a password.
        token_type="pin",  # nosec B106
        token_version=token_version,
    )
