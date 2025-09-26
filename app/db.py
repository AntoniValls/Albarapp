"""
Initializes the database engine and session factory (SQLAlchemy, async).
Also exposes the declarative Base and a dependency to obtain a DB session.
"""

from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession
from sqlalchemy.orm import DeclarativeBase
from .config import settings

engine = create_async_engine(settings.DB_URL, echo=False)
SessionLocal = async_sessionmaker(engine, expire_on_commit=False)

class Base(DeclarativeBase):
    """Declarative base for ORM models."""
    pass


async def get_session() -> AsyncSession:
    """FastAPI dependency that yields an async database session."""
    async with SessionLocal() as session:
        yield session
