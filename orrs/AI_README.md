# ORRS AI Next Version (CIPN + Rehab Week Plan + Chatbot)

## What’s new
- AI-ready database schema: `src/main/resources/db/schema_ai.sql`
- FastAPI AI service upgraded: `ai_service/`
  - `/ai/cipn/predict` (improved severity + confidence + explanation)
  - `/ai/rehab/weekly` (weekly plan generator: daily exercise + nutrition + safety)
- Optional OpenAI emotional-support chatbot (server-side): `/api/patient/chat`

## Setup (Localhost)
### 1) Database
1. Create a MySQL database (e.g., `orrs`).
2. Run your existing schema (JPA can create tables).
3. Import `src/main/resources/db/schema_ai.sql` (adds extra tables used by AI features).

### 2) Run Backend
`./mvnw spring-boot:run`

### 3) Run AI Service
From `orrs/ai_service`:
`python -m venv .venv`
Activate venv, then:
`pip install -r requirements.txt`
`uvicorn main:app --host 127.0.0.1 --port 8001`

Backend expects AI service URL:
- `app.ai.baseUrl=http://localhost:8001`

### 4) OpenAI Chatbot (optional)
Set environment variable:
- `OPENAI_API_KEY=...`

Then call:
- `POST /api/patient/chat` with `{ "message": "..." }`

> Keep your key server-side only. Never put it in frontend.
