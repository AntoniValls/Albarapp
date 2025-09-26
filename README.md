
# Albarapp Backend (MVP)

FastAPI backend for a simple two-party **delivery note (albarán)** workflow: a subcontractor creates/sends a delivery note, the contractor signs it, and both parties (plus their managers) receive copies. 

## Features (MVP)
- REST API with **FastAPI**.
- **Delivery notes** (header + line items).
- Simple **state machine**: `draft → pending_sign → signed` (plus `sent` and `rejected` for future use).
- Async **SQLAlchemy** models, SQLite by default (easy to switch to PostgreSQL).
- Auto-created tables on startup (dev-only convenience).
- Ready-to-extend with **auth**, **PDF generation**, and **signature provider integration**.

---

## Architecture & Tech
- **FastAPI** (web framework) + **Uvicorn** (ASGI server)
- **SQLAlchemy 2.0 (async)** (ORM) + **SQLite** (local dev) / PostgreSQL (prod)
- **Pydantic v2** for validation
- **pytest** for tests

Directory layout:
```
albarapp-backend/
├─ .venv/
├─ app/
│  ├─ __init__.py
│  ├─ config.py
│  ├─ db.py
│  ├─ models.py
│  ├─ schemas.py
│  ├─ main.py
│  └─ routers/
│     ├─ __init__.py
│     └─ delivery_notes.py
├─ tests/
|  ├─ smoke_test.py
│  └─ test_delivery_notes.py
├─ requirements.txt
├─ .env
├─ .gitignore
├─ albarapp.db
├─ alembic.ini
└─ README.md
```

---

## Prerequisites
- **Python 3.11**
- **Git**
- (Optional) **VS Code** with Python extension

---

## Quickstart (Windows PowerShell)

```powershell
# 1) Create and activate venv (Python 3.11)
python --version            # should be 3.11.x
python -m venv .venv
.\.venv\Scripts\Activate.ps1

# 2) Install dependencies
pip install --upgrade pip
pip install -r requirements.txt

# 3) Copy env template (for dev we keep SQLite)
Copy-Item .env.example .env

# 4) Run the server
python -m uvicorn app.main:app --reload

# 5) Open the API docs
# http://127.0.0.1:8000/docs
```

### Smoke Test

**curl (Windows):**
```bash
curl -X POST http://127.0.0.1:8000/delivery-notes ^
  -H "Content-Type: application/json" ^
  -d "{ "code":"DN-0001","contractor_id":10,"subcontractor_id":20,
        "site_name":"Main Square Site","date":"2025-09-26",
        "items":[ {"description":"Concrete H25","quantity":3.5,"unit":"m3","unit_price":95.0} ] }"

curl http://127.0.0.1:8000/delivery-notes
curl -X POST http://127.0.0.1:8000/delivery-notes/1/send
```

**Python script:**
```bash
python smoke_test.py
```

---

## Configuration

Environment variables (loaded via `pydantic-settings`). For local dev, `.env` is read automatically.

```
APP_NAME="Albarapp API"
DB_URL="sqlite+aiosqlite:///./albarapp.db"
```

- To switch to **PostgreSQL** later:
  ```
  DB_URL="postgresql+asyncpg://USER:PASS@localhost:5432/albarapp"
  ```
  And install: `pip install asyncpg`

---

## API Overview (MVP)

- `GET /` — Health check
- `POST /delivery-notes` — Create a delivery note (+ items)
- `GET /delivery-notes` — List delivery notes
- `POST /delivery-notes/{delivery_note_id}/send` — Transition to `pending_sign`

Open **/docs** for interactive OpenAPI.

---

## Learning Path (what to add next)

1. **Auth**: JWT + role-based authorization (contractor/subcontractor/admin).
2. **PDF generation**: WeasyPrint or ReportLab + SHA-256 hash footer.
3. **Signature workflow**: create a `SignatureProvider` interface + provider adapter (e.g., OTP).
4. **Notifications**: email on state transition (e.g., SendGrid/SMTP).
5. **PostgreSQL & Alembic**: migrations for production-readiness.
6. **Testing**: add integration tests with a temporary DB per test.

---

## Run tests

```bash
# In the activated venv
pytest -q
```

---

## Git: initialize and push a repo

### Option A) GitHub CLI (recommended)
```bash
git init
git add .
git commit -m "feat: initial FastAPI MVP for delivery notes"
# Replace with your GitHub username (memory says: AntoniValls). Adjust as needed.
gh repo create AntoniValls/albarapp-backend --public --source=. --remote=origin --push
```

### Option B) Manual (without gh CLI)
```bash
git init
git add .
git commit -m "feat: initial FastAPI MVP for delivery notes"
git branch -M main
# Create an empty repo on GitHub named "albarapp-backend", then:
git remote add origin https://github.com/<your-username>/albarapp-backend.git
git push -u origin main
```

---

## License

This repository uses the **MIT License** (see below).

---

## Security & Production Notes

- Don’t expose `DB_URL` with real credentials.
- Add authentication before exposing this API on the internet.
- Use HTTPS (TLS) in production.
- Enable structured logging and request IDs.
- Replace SQLite with PostgreSQL + Alembic migrations for multi-user/production scenarios.

---

## MIT License

Copyright (c) 2025

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
