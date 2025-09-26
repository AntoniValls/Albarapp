"""
Defines Pydantic models for request/response validation and serialization.
These are the shapes your API accepts and returns.
"""

from pydantic import BaseModel, Field
from datetime import date
from typing import List, Optional, Literal

class DeliveryItemIn(BaseModel):
    """Payload for creating a delivery item."""
    description: str
    quantity: float
    unit: str
    unit_price: float


class DeliveryItemOut(DeliveryItemIn):
    """Response shape for a delivery item."""
    id: int

    class Config:
        from_attributes = True  # replaces 'orm_mode' in Pydantic v2


class DeliveryNoteIn(BaseModel):
    """Payload for creating a delivery note with items."""
    code: str = Field(..., max_length=50)
    contractor_id: int
    subcontractor_id: int
    site_name: str
    date: date
    notes: Optional[str] = None
    items: List[DeliveryItemIn] = []


class DeliveryNoteOut(BaseModel):
    """Response shape for a delivery note including items."""
    id: int
    code: str
    site_name: str
    date: date
    state: Literal["draft", "sent", "pending_sign", "signed", "rejected"]
    notes: Optional[str]
    items: List[DeliveryItemOut] = []

    class Config:
        from_attributes = True
