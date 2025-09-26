"""
Defines the database schema using SQLAlchemy ORM models.
This file contains the core entities for the MVP:
- DeliveryNote: A delivery note (albarán) with header information.
- DeliveryItem: A line item belonging to a delivery note.
"""

from sqlalchemy import String, Integer, ForeignKey, Date, Enum, Numeric, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship
from .db import Base
import enum
from datetime import date


class DeliveryState(str, enum.Enum):
    """State machine for a delivery note lifecycle."""
    draft = "draft"
    sent = "sent"
    pending_sign = "pending_sign"
    signed = "signed"
    rejected = "rejected"


class DeliveryNote(Base):
    """Represents a delivery note header (who, where, when, status)."""

    __tablename__ = "delivery_notes"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    code: Mapped[str] = mapped_column(String(50), unique=True, index=True)

    contractor_id: Mapped[int] = mapped_column(Integer, index=True)
    subcontractor_id: Mapped[int] = mapped_column(Integer, index=True)

    site_name: Mapped[str] = mapped_column(String(120))
    date: Mapped[date] = mapped_column(Date)

    state: Mapped[DeliveryState] = mapped_column(Enum(DeliveryState), default=DeliveryState.draft)
    notes: Mapped[str | None] = mapped_column(Text, default=None)

    items: Mapped[list["DeliveryItem"]] = relationship(
        back_populates="delivery_note",
        cascade="all, delete-orphan",
        lazy="selectin",
    )


class DeliveryItem(Base):
    """Represents a single line item within a delivery note."""

    __tablename__ = "delivery_items"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    delivery_note_id: Mapped[int] = mapped_column(
        ForeignKey("delivery_notes.id", ondelete="CASCADE"),
        index=True,
    )
    description: Mapped[str] = mapped_column(String(200))
    quantity: Mapped[float] = mapped_column(Numeric(10, 2))
    unit: Mapped[str] = mapped_column(String(20))
    unit_price: Mapped[float] = mapped_column(Numeric(10, 2))

    delivery_note: Mapped[DeliveryNote] = relationship(back_populates="items")
