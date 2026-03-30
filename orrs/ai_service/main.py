from fastapi import FastAPI
from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any
import json, os, math, random
from datetime import date, timedelta

app = FastAPI(title="ORRS AI Service", version="0.2.0")

DATA_DIR = os.path.join(os.path.dirname(__file__), "data")
EX_LIB = json.load(open(os.path.join(DATA_DIR, "exercise_library.json"), "r", encoding="utf-8"))
NU_LIB = json.load(open(os.path.join(DATA_DIR, "nutrition_library.json"), "r", encoding="utf-8"))

# -------------------------
# CIPN MODEL (Hybrid)
# -------------------------

class CIPNRequest(BaseModel):
    # Required (existing)
    neuropathyScore: int = Field(ge=0, le=10)
    painScore: int = Field(ge=0, le=10)
    numbnessScore: int = Field(ge=0, le=10)
    tugTimeSeconds: Optional[float] = None

    # Optional enrichers (v2)
    age: Optional[int] = Field(default=None, ge=0, le=120)
    baselineNeuropathy: Optional[bool] = None
    hasDiabetes: Optional[bool] = None
    chemoAgents: Optional[str] = None  # comma-separated
    lastTotalScore: Optional[int] = None  # previous (neuropathy+pain+numbness)

class CIPNResponse(BaseModel):
    # Backward compatible fields (Java expects these)
    severity: str
    label: int
    confidence: float
    explanation: List[str]

    # New fields (frontend can show later)
    ctcaeGrade: int
    riskProb14d: float
    trend: str
    topFactors: List[str]

def _ctcae_grade(total_score: int, tug: Optional[float]) -> int:
    # Simple, defendable mapping (demo-grade)
    # Grade reflects symptoms + functional impairment signal from TUG.
    if total_score == 0:
        return 0
    grade = 1
    if total_score >= 7:
        grade = 2
    if total_score >= 15:
        grade = 3
    # functional impairment bump if very slow TUG
    if tug is not None and tug >= 20:
        grade = max(grade, 3)
    return grade

def _severity_from_grade(g: int) -> str:
    return ["NONE", "MILD", "MODERATE", "SEVERE", "SEVERE"][min(max(g,0),4)]

def _risk_logistic(x: float) -> float:
    return 1.0 / (1.0 + math.exp(-x))

HIGH_RISK_AGENTS = {"PACLITAXEL","DOCETAXEL","OXALIPLATIN","CISPLATIN","CARBOPLATIN","VINCRISTINE","BORTEZOMIB"}

@app.post("/ai/cipn/predict", response_model=CIPNResponse)
def predict_cipn(req: CIPNRequest):
    total = int(req.neuropathyScore + req.painScore + req.numbnessScore)

    # CTCAE-like grade (rule layer)
    grade = _ctcae_grade(total, req.tugTimeSeconds)
    severity = _severity_from_grade(grade)
    label = 1 if severity in ("MODERATE", "SEVERE") else 0

    # Risk model (lightweight ML-like scoring)
    x = -1.2
    x += 0.18 * total
    if req.tugTimeSeconds is not None:
        x += 0.04 * max(0.0, req.tugTimeSeconds - 12.0)
    if req.age is not None:
        x += 0.015 * max(0, req.age - 45)
    if req.baselineNeuropathy:
        x += 0.6
    if req.hasDiabetes:
        x += 0.4

    agents = set()
    if req.chemoAgents:
        agents = {a.strip().upper() for a in req.chemoAgents.split(",") if a.strip()}
        if agents & HIGH_RISK_AGENTS:
            x += 0.55

    risk = float(_risk_logistic(x))
    # confidence: higher when more signals present
    signals = 3 + (1 if req.tugTimeSeconds is not None else 0) + (1 if req.chemoAgents else 0)
    confidence = float(min(0.97, 0.55 + (signals * 0.05) + (total / 40.0)))

    # trend (simple delta if previous provided)
    trend = "STABLE"
    if req.lastTotalScore is not None:
        delta = total - int(req.lastTotalScore)
        if delta >= 2:
            trend = "WORSENING"
        elif delta <= -2:
            trend = "IMPROVING"

    top = []
    top.append(f"Total symptom score={total}")
    if req.tugTimeSeconds is not None and req.tugTimeSeconds > 13.5:
        top.append(f"TUG {req.tugTimeSeconds:.1f}s suggests fall risk")
    if agents & HIGH_RISK_AGENTS:
        top.append("High-risk chemo agent exposure")
    if req.hasDiabetes:
        top.append("Diabetes increases neuropathy risk")
    if req.baselineNeuropathy:
        top.append("Baseline neuropathy increases risk")

    explanation = [
        f"neuropathyScore={req.neuropathyScore}",
        f"painScore={req.painScore}",
        f"numbnessScore={req.numbnessScore}",
        f"ctcaeGrade={grade}",
        f"riskProb14d={risk:.2f}",
        f"trend={trend}",
    ]

    return CIPNResponse(
        severity=severity,
        label=label,
        confidence=confidence,
        explanation=explanation,
        ctcaeGrade=grade,
        riskProb14d=risk,
        trend=trend,
        topFactors=top[:5],
    )

# -------------------------
# REHAB WEEK PLAN GENERATOR
# -------------------------

class RehabWeeklyRequest(BaseModel):
    cancerType: Optional[str] = None
    cancerStage: Optional[str] = None
    treatmentType: Optional[str] = None
    dominantHand: Optional[str] = None
    comorbidities: Optional[str] = None

    # from latest assessment
    ctcaeGrade: int = Field(ge=0, le=4)
    tugTimeSeconds: Optional[float] = None
    dexTimeSeconds: Optional[float] = None
    dexErrors: Optional[int] = 0
    neuropathyScore: int = Field(ge=0, le=10)
    painScore: int = Field(ge=0, le=10)
    numbnessScore: int = Field(ge=0, le=10)

class RehabWeeklyResponse(BaseModel):
    schemaVersion: int = 1
    rehabLevel: str
    doctorApprovalRequired: bool
    safetyNotes: List[str]
    weekStart: str
    weekPlan: List[Dict[str, Any]]

def _rehab_level(grade: int, tug: Optional[float], pain: int) -> str:
    if grade >= 3 or (tug is not None and tug >= 20) or pain >= 8:
        return "BASIC"
    if grade == 2 or (tug is not None and tug >= 13.5):
        return "INTERMEDIATE"
    return "ADVANCED"

def _doctor_required(level: str) -> bool:
    return level == "BASIC"

def _pick_ex(code: str):
    for e in EX_LIB["exercises"]:
        if e["code"] == code:
            return e
    raise KeyError(code)

def _day_template(level: str, fall_risk: bool, cipn: bool):
    # Basic structure
    items = []
    items.append(_pick_ex("WARMUP_MARCH"))
    if cipn:
        items.append(_pick_ex("SENSORY_TEXTURE"))
        items.append(_pick_ex("DEXTERITY_FINGER_TAPS"))
    # balance & strength (scaled)
    items.append(_pick_ex("BALANCE_TANDEM"))
    items.append(_pick_ex("STRENGTH_SIT_TO_STAND"))
    items.append(_pick_ex("AEROBIC_WALK"))
    items.append(_pick_ex("STRETCH_CALF"))

    # if high fall risk, reduce balance challenge and aerobic minutes
    if fall_risk:
        for it in items:
            if it.get("code") == "BALANCE_TANDEM":
                it = it
        # handled in formatting stage
    return items

def _format_ex(e: Dict[str, Any], fall_risk: bool) -> Dict[str, Any]:
    out = {
        "category": e["category"],
        "name": e["name"],
        "instructions": e["instructions"],
    }
    if "sets" in e: out["sets"] = e["sets"]
    if "reps" in e: out["reps"] = e["reps"]
    if "base_minutes" in e: out["durationMin"] = e["base_minutes"]
    # safety adjustments
    flags=[]
    if fall_risk and "fall_risk_sensitive" in e.get("tags", []):
        flags.append("Use support / caregiver nearby")
        if "durationMin" in out and out["durationMin"] > 10:
            out["durationMin"] = max(6, int(out["durationMin"] * 0.6))
    if flags:
        out["safetyFlags"] = flags
    return out

@app.post("/ai/rehab/weekly", response_model=RehabWeeklyResponse)
def generate_week(req: RehabWeeklyRequest):
    total = req.neuropathyScore + req.painScore + req.numbnessScore
    cipn = req.ctcaeGrade >= 1 or total >= 3
    fall_risk = (req.tugTimeSeconds is not None and req.tugTimeSeconds >= 13.5)

    level = _rehab_level(req.ctcaeGrade, req.tugTimeSeconds, req.painScore)
    doctor_req = _doctor_required(level)

    safety = []
    if fall_risk:
        safety.append("Fall risk detected: do balance tasks near support; consider caregiver.")
    if req.painScore >= 7:
        safety.append("High pain: reduce intensity; stop if pain increases sharply.")
    if req.ctcaeGrade >= 3:
        safety.append("Severe symptoms: clinician review recommended.")
    if req.cancerType and "breast" in req.cancerType.lower() and req.treatmentType:
        safety.append("If post-breast surgery: avoid heavy upper-limb load early; monitor swelling.")

    week_start = date.today()
    week = []
    for i in range(7):
        d = week_start + timedelta(days=i)
        title = "Recovery + Balance"
        if i in (1,3,5):
            title = "Strength + Aerobic"
        if i == 6:
            title = "Active Recovery"

        base_items = _day_template(level, fall_risk, cipn)
        # small variety: shuffle non-warmup/stretch
        core = base_items[1:-1]
        random.shuffle(core)
        items = [base_items[0]] + core[:4] + [base_items[-1]]

        exercises=[_format_ex(e, fall_risk) for e in items]

        nutrition=[]
        # Always include protein + hydration + 1 rotating guideline
        nutrition.append({"goal":"PROTEIN","text":NU_LIB["guidelines"][0]["text"],"suggestion":random.choice(NU_LIB["meal_ideas"])})
        nutrition.append({"goal":"HYDRATION","text":NU_LIB["guidelines"][1]["text"],"suggestion":"Sip regularly; include soups/coconut water if tolerated."})
        nutrition.append({"goal":"FIBER","text":NU_LIB["guidelines"][2]["text"],"suggestion":"Add fruit + oats; or vegetables + brown rice."})

        week.append({
            "day": i+1,
            "date": d.isoformat(),
            "title": title,
            "rpeTarget": 3 if level=="BASIC" else (4 if level=="INTERMEDIATE" else 5),
            "exercises": exercises,
            "nutrition": nutrition,
            "selfCheck": {
                "stopIf": ["chest pain", "severe dizziness", "new foot wounds", "sudden weakness"],
                "symptomLimit": "If neuropathy/pain increases by 2+ points next day, reduce intensity 20%."
            }
        })

    return RehabWeeklyResponse(
        rehabLevel=level,
        doctorApprovalRequired=doctor_req,
        safetyNotes=safety or ["Follow the plan at comfortable intensity; avoid pushing through sharp pain."],
        weekStart=week_start.isoformat(),
        weekPlan=week
    )
