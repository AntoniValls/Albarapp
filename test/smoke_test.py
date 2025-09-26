"""
Minimal end-to-end smoke test against the running FastAPI server.
"""
import requests

BASE = "http://127.0.0.1:8000"

print("Health:", requests.get(f"{BASE}/").json())

payload = {
    "code": "DN-0002",
    "contractor_id": 10,
    "subcontractor_id": 20,
    "site_name": "Main Square Site",
    "date": "2025-09-26",
    "items": [{"description": "Concrete H25", "quantity": 3.5, "unit": "m3", "unit_price": 95.0}],
}
created = requests.post(f"{BASE}/delivery-notes", json=payload).json()
print("Created:", created)

note_id = created["id"]
print("List:", requests.get(f"{BASE}/delivery-notes").json())
print("Send:", requests.post(f"{BASE}/delivery-notes/{note_id}/send").json())
