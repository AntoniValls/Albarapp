
"""
Basic tests using FastAPI's TestClient to validate core endpoints.
Run with: pytest -q
"""
from __future__ import annotations

from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_health():
    resp = client.get("/")
    assert resp.status_code == 200
    assert resp.json().get("ok") is True


def test_create_and_send_delivery_note():
    payload = {
        "code": "DN-TEST-001",
        "contractor_id": 1,
        "subcontractor_id": 2,
        "site_name": "Test Site",
        "date": "2025-09-26",
        "items": [{"description": "Concrete H25", "quantity": 1.5, "unit": "m3", "unit_price": 90.0}],
    }
    r_create = client.post("/delivery-notes", json=payload)
    assert r_create.status_code == 200
    created = r_create.json()
    assert created["code"] == payload["code"]
    note_id = created["id"]

    r_list = client.get("/delivery-notes")
    assert r_list.status_code == 200
    assert any(dn["id"] == note_id for dn in r_list.json())

    r_send = client.post(f"/delivery-notes/{note_id}/send")
    assert r_send.status_code == 200
    assert r_send.json()["state"] == "pending_sign"
