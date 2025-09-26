"""
Application entrypoint for the FastAPI server.
It wires up database initialization and includes the routers.
"""

from fastapi import FastAPI
from .db import engine, Base
from .routers import delivery_notes

app = FastAPI(title="Albarapp API (MVP)")


@app.on_event("startup")
async def on_startup():
    """Create database tables on startup (dev-only convenience)."""
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)


# Routers
app.include_router(delivery_notes.router)


@app.get("/")
def root():
    """Health check endpoint."""
    return {"ok": True, "service": "albarapp"}
