"""
Exposes REST endpoints to create, list, and transition delivery notes.
"""

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from ..db import get_session
from .. import models, schemas

router = APIRouter(prefix="/delivery-notes", tags=["delivery_notes"])

@router.post("", response_model=schemas.DeliveryNoteOut)
async def create_delivery_note(
    payload: schemas.DeliveryNoteIn,
    db: AsyncSession = Depends(get_session),
):
    """
    Create a delivery note with optional items.
    Returns the created entity including generated IDs.
    """
    dn = models.DeliveryNote(
        code=payload.code,
        contractor_id=payload.contractor_id,
        subcontractor_id=payload.subcontractor_id,
        site_name=payload.site_name,
        date=payload.date,
        notes=payload.notes,
    )
    for item in payload.items:
        dn.items.append(
            models.DeliveryItem(
                description=item.description,
                quantity=item.quantity,
                unit=item.unit,
                unit_price=item.unit_price,
            )
        )
    db.add(dn)
    await db.commit()
    await db.refresh(dn)
    return dn


@router.get("", response_model=list[schemas.DeliveryNoteOut])
async def list_delivery_notes(db: AsyncSession = Depends(get_session)):
    """
    List all delivery notes.
    In a real app, you will filter by role/company and paginate results.
    """
    res = await db.execute(select(models.DeliveryNote))
    return res.scalars().unique().all()


@router.post("/{delivery_note_id}/send", response_model=schemas.DeliveryNoteOut)
async def send_delivery_note(delivery_note_id: int, db: AsyncSession = Depends(get_session)):
    """
    Transition a delivery note from draft/sent to pending_sign.
    This simulates the step where the contractor will be notified to sign.
    """
    dn = await db.get(models.DeliveryNote, delivery_note_id)
    if not dn:
        raise HTTPException(404, "Delivery note not found")
    dn.state = models.DeliveryState.pending_sign
    await db.commit()
    await db.refresh(dn)
    return dn
