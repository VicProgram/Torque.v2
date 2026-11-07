from functools import lru_cache
from typing import Literal

from pydantic import model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict

DEFAULT_SECRET_KEY = "change-me-to-a-random-secret-key-at-least-32-chars"  # nosec B105
_KNOWN_PLACEHOLDER_SECRETS = {
    DEFAULT_SECRET_KEY,
    "replace-with-a-random-secret-at-least-32-chars",
    "your-secret-key",
    "changeme",
}


class Settings(BaseSettings):
    """Application configuration with production safety checks.

    Every value can be overridden with an environment variable using the
    upper-case name (for example ``DATABASE_URL`` or ``FORCE_HTTPS``).
    """

    environment: Literal["development", "test", "production"] = "development"
    database_url: str = "sqlite:///./torque.db"
    redis_url: str = "redis://localhost:6379/0"

    secret_key: str = DEFAULT_SECRET_KEY
    algorithm: str = "HS256"
    jwt_issuer: str = "torque-api"
    jwt_audience: str = "torque-clients"
    # Used to build an indexed HMAC lookup for PINs without storing them plaintext.
    # When empty, ``secret_key`` is used. Set a dedicated value in production.
    pin_pepper: str = ""

    access_token_expire_minutes: int = 60
    pin_token_expire_minutes: int = 480

    debug: bool = False
    log_level: str = "INFO"
    allowed_origins: str = "http://localhost:8080,http://localhost:3000"
    trusted_hosts: str = "*"
    force_https: bool = False
    enable_docs: bool = True
    max_request_bytes: int = 10 * 1024 * 1024

    # Brute-force / abuse controls.
    login_rate_limit_attempts: int = 10
    login_rate_limit_window_seconds: int = 300
    login_lockout_threshold: int = 5
    login_lockout_minutes: int = 15

    password_min_length: int = 8
    password_max_length: int = 128

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    @property
    def origins_list(self) -> list[str]:
        return [o.strip() for o in self.allowed_origins.split(",") if o.strip()]

    @property
    def trusted_hosts_list(self) -> list[str]:
        return [h.strip() for h in self.trusted_hosts.split(",") if h.strip()]

    @property
    def pin_pepper_value(self) -> str:
        return self.pin_pepper or self.secret_key

    @model_validator(mode="after")
    def _validate_security_posture(self) -> "Settings":
        origins = self.origins_list
        if "*" in origins:
            if self.environment == "production":
                raise ValueError("ALLOWED_ORIGINS cannot contain '*' in production")
            # A wildcard origin combined with credentials is rejected by browsers
            # and hides configuration mistakes. Require explicit origins instead.
            raise ValueError("ALLOWED_ORIGINS must list explicit origins, not '*'")

        if self.environment == "production":
            if (
                self.secret_key in _KNOWN_PLACEHOLDER_SECRETS
                or len(self.secret_key) < 32
            ):
                raise ValueError(
                    "SECRET_KEY must be a unique random value of at least 32 chars in production"
                )
            if self.debug:
                raise ValueError("DEBUG must be false in production")
            if not self.trusted_hosts_list or "*" in self.trusted_hosts_list:
                raise ValueError("TRUSTED_HOSTS must list explicit hosts in production")
            if not self.force_https:
                raise ValueError("FORCE_HTTPS must be true in production")

        if self.password_min_length < 8:
            raise ValueError("PASSWORD_MIN_LENGTH must be at least 8")
        if self.login_lockout_threshold < 3:
            raise ValueError("LOGIN_LOCKOUT_THRESHOLD must be at least 3")
        return self


@lru_cache
def get_settings() -> Settings:
    return Settings()
