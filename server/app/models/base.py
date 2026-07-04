import uuid
from datetime import datetime, timezone

from sqlalchemy import Column, DateTime, CHAR, TypeDecorator
from sqlalchemy.dialects.postgresql import UUID as PG_UUID

from app.database import Base


class GUID(TypeDecorator):
    """Platform-independent UUID type.
    Uses PostgreSQL UUID when available, stores as CHAR(36) on SQLite.
    """
    impl = CHAR
    cache_ok = True

    def load_dialect_impl(self, dialect):
        if dialect.name == "postgresql":
            return dialect.type_descriptor(PG_UUID())
        return dialect.type_descriptor(CHAR(36))

    def process_bind_param(self, value, dialect):
        if value is None:
            return value
        if dialect.name == "postgresql":
            return value
        return str(value)

    def process_result_value(self, value, dialect):
        if value is None:
            return value
        if isinstance(value, uuid.UUID):
            return value
        return uuid.UUID(value)


class TimestampMixin:
    creado_en = Column(
        DateTime(timezone=True),
        default=lambda: datetime.now(timezone.utc),
        nullable=False,
    )
    actualizado_en = Column(
        DateTime(timezone=True),
        default=lambda: datetime.now(timezone.utc),
        onupdate=lambda: datetime.now(timezone.utc),
        nullable=False,
    )


class UUIDMixin:
    id = Column(
        GUID(),
        primary_key=True,
        default=uuid.uuid4,
        index=True,
    )
