"""
Holds application configuration loaded from environment variables.
Use this module to centralize settings such as database URLs and secrets.
"""

from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    APP_NAME: str = "Albarapp API"
    # For local development you can start with SQLite; switch to Postgres later.
    DB_URL: str = "sqlite+aiosqlite:///./albarapp.db"

    class Config:
        env_file = ".env"


settings = Settings()
