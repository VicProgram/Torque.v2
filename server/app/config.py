from functools import lru_cache

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    database_url: str = "sqlite:///./torque.db"
    redis_url: str = "redis://localhost:6379/0"
    secret_key: str = "change-me-to-a-random-secret-key-at-least-32-chars"
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 1440
    pin_token_expire_minutes: int = 10080
    debug: bool = False
    log_level: str = "INFO"
    allowed_origins: str = "http://localhost:8080,http://localhost:3000"

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8"}

    @property
    def origins_list(self) -> list[str]:
        return [o.strip() for o in self.allowed_origins.split(",") if o.strip()]


@lru_cache
def get_settings() -> Settings:
    return Settings()
