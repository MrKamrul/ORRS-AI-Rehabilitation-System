# ORRS – Onco-Rehabilitation Specialist System (Phase-3 Demo)

This repo contains a **ready-to-run full-stack ORRS**:
- **Backend**: Spring Boot (Java) + MySQL (XAMPP)
- **Frontend**: React (Vite) + Tailwind
- **AI Service**: FastAPI (Python) – optional, with safe fallback to rule-based prediction

## 1) Database (XAMPP / MySQL)
Create the database:

```sql
CREATE DATABASE orrs_db;
```

Then start MySQL from XAMPP.

> Backend uses:
> - URL: `jdbc:mysql://localhost:3306/orrs_db`
> - user: `root`
> - password: empty (change in `src/main/resources/application.properties` if needed)

## 2) Backend (Spring Boot)
From the project root:

```bash
cd orrs
./mvnw spring-boot:run
```

Backend runs on: `http://localhost:8080`

### JWT Setup
Edit this in `orrs/src/main/resources/application.properties`:

- `app.jwt.secret` **must be a long random secret** (32+ characters)

## 3) AI Service (Optional)
If you want AI predictions:

```bash
cd orrs/ai_service
python -m venv .venv
# Windows: .venv\Scripts\activate
# Mac/Linux: source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

AI runs on: `http://localhost:8000`

If AI is OFF, backend automatically falls back to rule-based prediction.

## 4) Frontend (React)
```bash
cd orrs/frontend
npm install
npm run dev
```

Frontend runs on: `http://localhost:5173`

## 5) Demo Flow (for defense)
1. Register **PATIENT** and **DOCTOR** accounts in the UI
2. Login as PATIENT → submit assessment (CIPN + gait + dexterity)
3. ORRS returns:
   - safety triage result
   - CIPN prediction
   - rehab plan
4. Login as DOCTOR → see patient list and sessions

## API Quick Test
- `POST /api/auth/register`
- `POST /api/auth/login` (returns JWT)
- `POST /api/patient/assessment` (Bearer token required)

---
### Clinical Note (for viva)
This is a **Phase-3 prototype**:
- AI output is **advisory**
- A **clinical triage gate** prevents unsafe automatic rehab in high-risk cases
