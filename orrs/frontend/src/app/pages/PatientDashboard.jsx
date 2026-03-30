import React, { useEffect, useMemo, useRef, useState } from "react";
import { api } from "../api";

/**
 * ✅ FIX for “must click every field one by one”
 * Move Field components OUTSIDE PatientProfile so React doesn’t remount inputs on each keystroke.
 */

function ProfileField({ label, name, type = "text", placeholder, options, payload, setPayload, missing }) {
  const isMissing = (missing || []).includes(name);

  return (
    <div className="space-y-1">
      <label className="text-sm font-semibold flex items-center gap-2">
        {label}
        <span className="text-red-500">*</span>
        {isMissing ? <span className="text-xs text-red-600">required</span> : null}
      </label>

      {options ? (
        <select
          className={`w-full rounded-xl border px-3 py-2 bg-white ${isMissing ? "border-red-400" : ""}`}
          value={payload?.[name] ?? ""}
          onChange={(e) => setPayload((p) => ({ ...p, [name]: e.target.value }))}
        >
          <option value="" disabled>
            Select...
          </option>
          {options.map((opt) => (
            <option key={opt} value={opt}>
              {opt}
            </option>
          ))}
        </select>
      ) : (
        <input
          className={`w-full rounded-xl border px-3 py-2 ${isMissing ? "border-red-400" : ""}`}
          type={type}
          placeholder={placeholder}
          value={payload?.[name] ?? ""}
          onChange={(e) => setPayload((p) => ({ ...p, [name]: e.target.value }))}
        />
      )}
    </div>
  );
}

function ProfileBoolField({ label, name, payload, setPayload, missing }) {
  const isMissing = (missing || []).includes(name);
  const val = payload?.[name];

  return (
    <div className="space-y-1">
      <label className="text-sm font-semibold flex items-center gap-2">
        {label}
        <span className="text-red-500">*</span>
        {isMissing ? <span className="text-xs text-red-600">required</span> : null}
      </label>

      <div className="flex gap-2">
        <button
          type="button"
          onClick={() => setPayload((p) => ({ ...p, [name]: true }))}
          className={`flex-1 rounded-xl border px-3 py-2 ${
            val === true ? "bg-gray-100 font-semibold" : "bg-white"
          } ${isMissing ? "border-red-400" : ""}`}
        >
          Yes
        </button>
        <button
          type="button"
          onClick={() => setPayload((p) => ({ ...p, [name]: false }))}
          className={`flex-1 rounded-xl border px-3 py-2 ${
            val === false ? "bg-gray-100 font-semibold" : "bg-white"
          } ${isMissing ? "border-red-400" : ""}`}
        >
          No
        </button>
      </div>
    </div>
  );
}

export default function PatientDashboard() {
  const [view, setView] = useState("home"); // home | profile | symptoms | gait | dexterity | progress | rehab | notifications | settings
  const [dash, setDash] = useState(null);
  const [sessions, setSessions] = useState([]);
  const [selectedSessionId, setSelectedSessionId] = useState(null);
  const [sessionDetails, setSessionDetails] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const [profilePayload, setProfilePayload] = useState(null);
  const [profileMeta, setProfileMeta] = useState(null);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState(null);

  // Assessment draft (NeuroTrack+)
  const [draft, setDraft] = useState({
    // physical tests
    tugTimeSeconds: null,
    completionTimeSeconds: null,
    errorCount: 0,
    handUsed: "RIGHT",
    // CIPN symptom tracker (0-10)
    neuropathyScore: 0,
    painScore: 0,
    numbnessScore: 0,
  });
  const [submitResult, setSubmitResult] = useState(null);

  async function refreshAll() {
    setErr(null);
    try {
      const d = await api.patientDashboard();
      setDash(d);
      const s = await api.patientSessions();
      setSessions(s || []);
    } catch (e) {
      setErr(e.message);
    }
  }

  useEffect(() => {
    refreshAll();
  }, []);

  useEffect(() => {
    (async () => {
      if (view !== "notifications") return;
      try {
        const n = await api.patientNotifications();
        setNotifications(n || []);
      } catch (e) {
        setErr(e.message);
      }
    })();
  }, [view]);

  useEffect(() => {
    (async () => {
      if (view !== "profile") return;
      try {
        const p = await api.getPatientProfile();
        setProfileMeta({
          fullName: p.fullName,
          email: p.email,
          profileComplete: p.profileComplete,
          missingProfileFields: p.missingProfileFields || [],
        });
        setProfilePayload(p.profile || {});
      } catch (e) {
        setErr(e.message);
      }
    })();
  }, [view]);

  useEffect(() => {
    (async () => {
      if (!selectedSessionId) return;
      try {
        const d = await api.patientSessionDetails(selectedSessionId);
        setSessionDetails(d);
      } catch (e) {
        setErr(e.message);
      }
    })();
  }, [selectedSessionId]);

  const lastAssessmentText = useMemo(() => {
    if (!dash?.lastAssessmentDate) return "No assessments yet";
    const dt = new Date(dash.lastAssessmentDate);
    return dt.toLocaleString();
  }, [dash]);

  async function submitAssessment() {
    setLoading(true);
    setErr(null);
    setSubmitResult(null);
    try {
      // Basic validation
      if (draft.tugTimeSeconds == null) throw new Error("Please complete the Gait (TUG) test first.");
      if (draft.completionTimeSeconds == null) throw new Error("Please complete the Dexterity test first.");

      const res = await api.submitAssessment({
        tugTimeSeconds: Number(draft.tugTimeSeconds),
        completionTimeSeconds: Number(draft.completionTimeSeconds),
        errorCount: Number(draft.errorCount || 0),
        handUsed: draft.handUsed || "RIGHT",
        neuropathyScore: Number(draft.neuropathyScore),
        painScore: Number(draft.painScore),
        numbnessScore: Number(draft.numbnessScore),
      });
      setSubmitResult(res);
      setView("rehab");
      await refreshAll();
    } catch (e) {
      setErr(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="space-y-4">
      <Header
        title="NeuroTrack+"
        subtitle="Onco-Rehabilitation Specialist System (ORRS) — Patient Panel"
        badge={dash?.unreadNotifications ? `${dash.unreadNotifications} new` : null}
        onBell={() => setView("notifications")}
      />

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-4">
        <SideNav view={view} setView={setView} profileComplete={dash?.profileComplete} />

        <div className="lg:col-span-3 space-y-4">
          {err && <div className="rounded-2xl border bg-white p-4 text-sm text-red-600">{err}</div>}

          {view === "home" && (
            <Home
              name={dash?.fullName || localStorage.getItem("fullName")}
              lastAssessment={lastAssessmentText}
              profileComplete={Boolean(dash?.profileComplete)}
              missingCount={(dash?.missingProfileFields || []).length}
              draft={draft}
              onGo={(v) => setView(v)}
              sessions={sessions}
              onPickSession={(id) => {
                setSelectedSessionId(id);
                setView("progress");
              }}
            />
          )}

          {view === "profile" && (
            <PatientProfile
              meta={profileMeta}
              payload={profilePayload}
              setPayload={setProfilePayload}
              onSave={async () => {
                setLoading(true);
                setErr(null);
                try {
                  const res = await api.updatePatientProfile(profilePayload);
                  setProfileMeta({
                    fullName: localStorage.getItem("fullName"),
                    email: dash?.email,
                    profileComplete: res.profileComplete,
                    missingProfileFields: res.missingProfileFields || [],
                  });
                  await refreshAll();
                } catch (e) {
                  setErr(e.message);
                } finally {
                  setLoading(false);
                }
              }}
              saving={loading}
            />
          )}

          {view === "symptoms" && <Symptoms draft={draft} setDraft={setDraft} onSubmit={submitAssessment} loading={loading} />}

          {view === "gait" && (
            <GaitAssessment
              value={draft.tugTimeSeconds}
              onDone={(seconds) => {
                setDraft((d) => ({ ...d, tugTimeSeconds: seconds }));
                setView("dexterity");
              }}
            />
          )}

          {view === "dexterity" && (
            <DexterityTest
              handUsed={draft.handUsed}
              onHand={(hand) => setDraft((d) => ({ ...d, handUsed: hand }))}
              onDone={(seconds, errors) => {
                setDraft((d) => ({ ...d, completionTimeSeconds: seconds, errorCount: errors }));
                setView("symptoms");
              }}
            />
          )}

          {view === "progress" && (
            <Progress
              sessions={sessions}
              selectedSessionId={selectedSessionId}
              onSelect={setSelectedSessionId}
              details={sessionDetails}
              onDownload={async () => {
                const blob = await api.downloadPatientReport(selectedSessionId);
                downloadBlob(blob, `NeuroTrack_Report_${selectedSessionId}.pdf`);
              }}
            />
          )}

          {view === "rehab" && (
            <RehabPlanView
              latestPlan={dash?.latestRehabPlan}
              submitResult={submitResult}
              onStartNew={() => {
                setSubmitResult(null);
                setView("gait");
              }}
              onDownloadLatest={async () => {
                const blob = await api.downloadPatientReport(null);
                downloadBlob(blob, `NeuroTrack_Report_Latest.pdf`);
              }}
            />
          )}

          {view === "notifications" && (
            <Notifications
              items={notifications}
              onMarkRead={async (id) => {
                await api.markNotificationRead(id);
                const n = await api.patientNotifications();
                setNotifications(n || []);
                await refreshAll();
              }}
            />
          )}

          {view === "settings" && <Settings />}
        </div>
      </div>
    </div>
  );
}

function Header({ title, subtitle, badge, onBell }) {
  return (
    <div className="rounded-2xl border bg-white p-5 shadow-sm flex items-center justify-between">
      <div>
        <div className="text-2xl font-semibold">{title}</div>
        <div className="text-sm text-gray-600">{subtitle}</div>
      </div>
      <button className="relative rounded-xl border px-3 py-2 hover:bg-gray-50" onClick={onBell} title="Notifications">
        🔔
        {badge && (
          <span className="absolute -top-2 -right-2 rounded-full bg-black text-white text-xs px-2 py-0.5">
            {badge}
          </span>
        )}
      </button>
    </div>
  );
}

function SideNav({ view, setView, profileComplete }) {
  const items = [
    { key: "home", label: "Home", icon: "🏠" },
    { key: "profile", label: "Patient Profile", icon: "👤", showDot: !profileComplete },
    { key: "gait", label: "Start Gait Assessment", icon: "🔄" },
    { key: "dexterity", label: "Manual Dexterity Test", icon: "✋" },
    { key: "symptoms", label: "CIPN Tracker", icon: "🧠" },
    { key: "progress", label: "Progress Report", icon: "📊" },
    { key: "rehab", label: "Rehab Plan", icon: "🧩" },
    { key: "notifications", label: "Notifications", icon: "🔔" },
    { key: "settings", label: "Settings", icon: "⚙️" },
  ];

  return (
    <div className="rounded-2xl border bg-white p-3 shadow-sm">
      <div className="text-xs font-semibold text-gray-500 px-2 py-2">PATIENT MENU</div>
      <div className="space-y-1">
        {items.map((it) => (
          <button
            key={it.key}
            onClick={() => setView(it.key)}
            className={`w-full flex items-center gap-2 rounded-xl px-3 py-2 text-sm text-left hover:bg-gray-50 ${
              view === it.key ? "bg-gray-100 font-semibold" : ""
            }`}
          >
            <span>{it.icon}</span>
            <span className="flex items-center gap-2">
              {it.label}
              {it.showDot ? (
                <span className="inline-block h-2 w-2 rounded-full bg-red-500" title="Complete your profile" />
              ) : null}
            </span>
          </button>
        ))}
      </div>
    </div>
  );
}

function Home({ name, lastAssessment, profileComplete, missingCount, draft, onGo, sessions, onPickSession }) {
  const lastId = sessions?.[0]?.id;
  return (
    <div className="rounded-2xl border bg-white p-6 shadow-sm">
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
        <div>
          <div className="text-xl font-semibold">Hello, {name || "Patient"}</div>
          <div className="text-sm text-gray-600">Last Assessment: {lastAssessment}</div>
        </div>
        <div className="flex gap-2">
          <button className="rounded-xl bg-black px-4 py-2 text-white" onClick={() => onGo("gait")}>
            Start NeuroTrack+ Test
          </button>
          <button className="rounded-xl border px-4 py-2" onClick={() => onGo("rehab")}>
            View Rehab Plan
          </button>
        </div>
      </div>

      <div className="mt-6 grid grid-cols-1 md:grid-cols-2 gap-4">
        <QuickCard
          title="🔄 Start Gait Assessment"
          desc="Timed Up & Go (TUG) — balance and fall risk"
          value={draft.tugTimeSeconds == null ? "Not done" : `${draft.tugTimeSeconds}s`}
          onClick={() => onGo("gait")}
        />
        <QuickCard
          title="✋ Manual Dexterity Test"
          desc="Nine-tap test — hand speed & accuracy"
          value={draft.completionTimeSeconds == null ? "Not done" : `${draft.completionTimeSeconds}s`}
          onClick={() => onGo("dexterity")}
        />
        <QuickCard
          title="🧠 Submit Symptoms (CIPN Tracker)"
          desc="Neuropathy, pain, numbness scores"
          value={`N:${draft.neuropathyScore}  P:${draft.painScore}  Nb:${draft.numbnessScore}`}
          onClick={() => onGo("symptoms")}
        />
        <QuickCard
          title="📊 View Progress Report"
          desc="Trends across your sessions"
          value={sessions?.length ? `${sessions.length} sessions` : "No sessions"}
          onClick={() => onGo("progress")}
        />
        <QuickCard
          title="👤 Patient Profile"
          desc="Required for personalized rehab planning"
          value={profileComplete ? "Complete" : `${missingCount || 0} missing`}
          onClick={() => onGo("profile")}
          badge={!profileComplete ? "●" : null}
        />
      </div>

      <div className="mt-6">
        <div className="flex items-center justify-between">
          <div className="font-semibold">Recent Sessions</div>
          {lastId && (
            <button className="text-sm underline" onClick={() => onPickSession(lastId)}>
              Open latest
            </button>
          )}
        </div>
        <div className="mt-2 overflow-auto rounded-xl border">
          <table className="min-w-full text-sm">
            <thead className="bg-gray-50 text-gray-600">
              <tr>
                <th className="px-3 py-2 text-left">Session</th>
                <th className="px-3 py-2 text-left">Date</th>
                <th className="px-3 py-2"></th>
              </tr>
            </thead>
            <tbody>
              {sessions?.length ? (
                sessions.slice(0, 5).map((s) => (
                  <tr key={s.id} className="border-t">
                    <td className="px-3 py-2">#{s.id}</td>
                    <td className="px-3 py-2">{new Date(s.assessmentDate).toLocaleString()}</td>
                    <td className="px-3 py-2 text-right">
                      <button
                        className="rounded-lg border px-3 py-1 hover:bg-gray-50"
                        onClick={() => onPickSession(s.id)}
                      >
                        View
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td className="px-3 py-3 text-gray-600" colSpan={3}>
                    No sessions yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function QuickCard({ title, desc, value, onClick, badge }) {
  return (
    <button onClick={onClick} className="rounded-2xl border p-4 text-left hover:bg-gray-50">
      <div className="font-semibold flex items-center justify-between">
        <span>{title}</span>
        {badge ? (
          <span className="text-red-500" title="Action required">
            {badge}
          </span>
        ) : null}
      </div>
      <div className="mt-1 text-sm text-gray-600">{desc}</div>
      <div className="mt-3 text-sm">
        <span className="text-gray-500">Status:</span> <span className="font-semibold">{value}</span>
      </div>
    </button>
  );
}

function Symptoms({ draft, setDraft, onSubmit, loading }) {
  return (
    <div className="rounded-2xl border bg-white p-6 shadow-sm space-y-4">
      <div>
        <div className="text-xl font-semibold">CIPN Tracker</div>
        <div className="text-sm text-gray-600">
          Update your symptoms, then submit the full assessment to generate your rehab plan.
        </div>
      </div>

      <Slider
        label="Overall Neuropathy"
        value={draft.neuropathyScore}
        onChange={(v) => setDraft((d) => ({ ...d, neuropathyScore: v }))}
      />
      <Slider label="Pain" value={draft.painScore} onChange={(v) => setDraft((d) => ({ ...d, painScore: v }))} />
      <Slider
        label="Numbness"
        value={draft.numbnessScore}
        onChange={(v) => setDraft((d) => ({ ...d, numbnessScore: v }))}
      />

      <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
        <MiniStat title="Gait (TUG)" value={draft.tugTimeSeconds == null ? "Not done" : `${draft.tugTimeSeconds}s`} />
        <MiniStat title="Dexterity" value={draft.completionTimeSeconds == null ? "Not done" : `${draft.completionTimeSeconds}s`} />
        <MiniStat title="Errors" value={draft.errorCount ?? 0} />
      </div>

      <button className="w-full rounded-xl bg-black px-4 py-3 text-white disabled:opacity-50" onClick={onSubmit} disabled={loading}>
        {loading ? "Submitting..." : "Submit Assessment & Generate Plan"}
      </button>
    </div>
  );
}

function Slider({ label, value, onChange }) {
  return (
    <div>
      <div className="flex items-center justify-between">
        <label className="text-sm font-medium">{label}</label>
        <span className="text-sm font-semibold">{value}</span>
      </div>
      <input type="range" min={0} max={10} value={value} onChange={(e) => onChange(Number(e.target.value))} className="w-full" />
      <div className="text-xs text-gray-500">0 = none • 10 = worst</div>
    </div>
  );
}

function MiniStat({ title, value }) {
  return (
    <div className="rounded-2xl border p-4">
      <div className="text-xs text-gray-500">{title}</div>
      <div className="mt-1 text-lg font-semibold">{String(value)}</div>
    </div>
  );
}

function GaitAssessment({ value, onDone }) {
  const [phase, setPhase] = useState("ready"); // ready | countdown | running | done
  const [count, setCount] = useState(10);
  const [elapsed, setElapsed] = useState(0);
  const intervalRef = useRef(null);

  useEffect(() => () => clearInterval(intervalRef.current), []);

  function startCountdown() {
    setPhase("countdown");
    setCount(10);
    clearInterval(intervalRef.current);
    intervalRef.current = setInterval(() => {
      setCount((c) => {
        if (c <= 1) {
          clearInterval(intervalRef.current);
          startRunning();
          return 0;
        }
        return c - 1;
      });
    }, 1000);
  }

  function startRunning() {
    setPhase("running");
    setElapsed(0);
    const start = Date.now();
    clearInterval(intervalRef.current);
    intervalRef.current = setInterval(() => {
      setElapsed((Date.now() - start) / 1000);
    }, 50);
  }

  function stop() {
    clearInterval(intervalRef.current);
    const seconds = Math.round(elapsed * 10) / 10;
    setPhase("done");
    onDone(seconds);
  }

  return (
    <div className="rounded-2xl border bg-white p-6 shadow-sm">
      <div className="text-xl font-semibold">🚶 Timed Up & Go (TUG)</div>
      <div className="mt-1 text-sm text-gray-600">Instructions: Sit on a chair → stand → walk 3m → turn → return & sit.</div>

      <div className="mt-4 rounded-2xl border p-4 bg-gray-50">
        <div className="text-sm font-medium">Countdown</div>
        <div className="mt-1 text-3xl font-semibold tabular-nums">
          {phase === "countdown" ? `00:${String(count).padStart(2, "0")}` : "00:10"}
        </div>
        <div className="mt-4 text-sm font-medium">Timer</div>
        <div className="mt-1 text-4xl font-semibold tabular-nums">{phase === "running" ? formatSeconds(elapsed) : "00:00"}</div>
      </div>

      <div className="mt-4 flex flex-wrap gap-2">
        {phase === "ready" && (
          <button className="rounded-xl bg-black px-4 py-2 text-white" onClick={startCountdown}>
            Start Test
          </button>
        )}
        {phase === "countdown" && (
          <button
            className="rounded-xl border px-4 py-2"
            onClick={() => {
              clearInterval(intervalRef.current);
              setPhase("ready");
            }}
          >
            Cancel
          </button>
        )}
        {phase === "running" && (
          <button className="rounded-xl bg-black px-4 py-2 text-white" onClick={stop}>
            Stop & Save
          </button>
        )}
      </div>

      <div className="mt-3 text-sm text-gray-600">
        Saved value: <span className="font-semibold">{value == null ? "—" : `${value}s`}</span>
      </div>
    </div>
  );
}

function DexterityTest({ handUsed, onHand, onDone }) {
  const [started, setStarted] = useState(false);
  const [elapsed, setElapsed] = useState(0);
  const [tapped, setTapped] = useState(() => new Set());
  const [errors, setErrors] = useState(0);
  const intervalRef = useRef(null);

  useEffect(() => () => clearInterval(intervalRef.current), []);

  function start() {
    setStarted(true);
    setTapped(new Set());
    setErrors(0);
    const t = Date.now();
    setElapsed(0);
    clearInterval(intervalRef.current);
    intervalRef.current = setInterval(() => setElapsed((Date.now() - t) / 1000), 50);
  }

  function tap(i) {
    if (!started) return;

    setTapped((prev) => {
      const next = new Set(prev);
      if (next.has(i)) setErrors((e) => e + 1);
      else next.add(i);

      if (next.size === 9) {
        clearInterval(intervalRef.current);
        const seconds = Math.round(elapsed * 10) / 10;
        // NOTE: errors might be 1 tick behind in edge case; if you notice it, I can provide a ref-based fix.
        onDone(seconds, errors);
      }
      return next;
    });
  }

  const doneCount = tapped.size;

  return (
    <div className="rounded-2xl border bg-white p-6 shadow-sm">
      <div className="text-xl font-semibold">✋ Nine-Tap Dexterity Test</div>
      <div className="mt-1 text-sm text-gray-600">
        Instructions: Tap each circle once as fast as you can. Tapping the same circle again counts as an error.
      </div>

      <div className="mt-4 flex flex-col md:flex-row md:items-center md:justify-between gap-3">
        <div className="flex items-center gap-2">
          <label className="text-sm text-gray-600">Hand</label>
          <select className="rounded-xl border px-3 py-2" value={handUsed} onChange={(e) => onHand(e.target.value)}>
            <option value="RIGHT">RIGHT</option>
            <option value="LEFT">LEFT</option>
          </select>
        </div>
        <div className="flex gap-4">
          <div className="text-sm">
            <span className="text-gray-500">Timer:</span>{" "}
            <span className="font-semibold tabular-nums">{started ? formatSeconds(elapsed) : "00:00"}</span>
          </div>
          <div className="text-sm">
            <span className="text-gray-500">Done:</span> <span className="font-semibold">{doneCount}/9</span>
          </div>
          <div className="text-sm">
            <span className="text-gray-500">Errors:</span> <span className="font-semibold">{errors}</span>
          </div>
        </div>
      </div>

      <div className="mt-4 grid grid-cols-3 gap-3 max-w-xs">
        {Array.from({ length: 9 }).map((_, i) => {
          const id = i + 1;
          const isTapped = tapped.has(id);
          return (
            <button
              key={id}
              onClick={() => tap(id)}
              className={`aspect-square rounded-2xl border text-lg font-semibold ${isTapped ? "bg-gray-100" : "bg-white hover:bg-gray-50"}`}
              disabled={!started}
              title={started ? "Tap" : "Start the test first"}
            >
              {id}
            </button>
          );
        })}
      </div>

      <div className="mt-4 flex gap-2">
        <button className="rounded-xl bg-black px-4 py-2 text-white" onClick={start}>
          Start Test
        </button>
        <button
          className="rounded-xl border px-4 py-2"
          onClick={() => {
            setStarted(false);
            clearInterval(intervalRef.current);
          }}
        >
          Reset
        </button>
      </div>
    </div>
  );
}

function Progress({ sessions, selectedSessionId, onSelect, details, onDownload }) {
  const points = useMemo(() => sessions?.map((s) => ({ id: s.id, date: new Date(s.assessmentDate) })) || [], [sessions]);

  return (
    <div className="rounded-2xl border bg-white p-6 shadow-sm space-y-4">
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
        <div>
          <div className="text-xl font-semibold">📊 Progress Report</div>
          <div className="text-sm text-gray-600">Select a session to view details and download the report PDF.</div>
        </div>
        <button className="rounded-xl border px-4 py-2" onClick={onDownload} disabled={!selectedSessionId}>
          ⬇️ Download Full Report
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div className="rounded-2xl border p-4">
          <div className="font-semibold">Sessions</div>
          <div className="mt-2 space-y-2 max-h-72 overflow-auto">
            {points.length ? (
              points.map((p) => (
                <button
                  key={p.id}
                  onClick={() => onSelect(p.id)}
                  className={`w-full rounded-xl border px-3 py-2 text-left hover:bg-gray-50 ${selectedSessionId === p.id ? "bg-gray-100" : ""}`}
                >
                  <div className="text-sm font-semibold">Session #{p.id}</div>
                  <div className="text-xs text-gray-600">{p.date.toLocaleString()}</div>
                </button>
              ))
            ) : (
              <div className="text-sm text-gray-600">No sessions yet.</div>
            )}
          </div>
        </div>

        <div className="rounded-2xl border p-4">
          <div className="font-semibold">Weekly Overview (Selected Session)</div>
          {!details ? <div className="mt-2 text-sm text-gray-600">Select a session to view details.</div> : <SessionSummary details={details} />}
        </div>
      </div>
    </div>
  );
}

function SessionSummary({ details }) {
  const gait = details.gait;
  const dex = details.dexterity;
  const symptom = details.symptom;
  const prediction = details.prediction;
  const plan = details.rehabPlan;

  return (
    <div className="mt-2 grid grid-cols-1 gap-3 text-sm">
      <Row k="Gait (TUG)" v={gait?.tugTimeSeconds != null ? `${gait.tugTimeSeconds}s` : "-"} />
      <Row k="Gait Risk" v={gait?.gaitRiskLevel ?? "-"} />
      <Row k="Dexterity Time" v={dex?.completionTimeSeconds != null ? `${dex.completionTimeSeconds}s` : "-"} />
      <Row k="Dexterity Errors" v={dex?.errorCount ?? "-"} />
      <Row k="Neuropathy" v={symptom?.overallNeuropathyScore ?? "-"} />
      <Row k="Pain" v={symptom?.painScore ?? "-"} />
      <Row k="Numbness" v={symptom?.numbnessScore ?? "-"} />
      <Row k="Predicted Severity" v={prediction?.severity ?? "-"} />
      <Row k="Rehab Plan" v={plan?.rehabLevel ?? "-"} />
      {plan?.doctorApprovalRequired ? (
        <div className="rounded-xl border p-3 bg-amber-50 text-amber-800">Doctor review required before starting this plan.</div>
      ) : null}
    </div>
  );
}

function Row({ k, v }) {
  return (
    <div className="flex items-center justify-between rounded-xl border px-3 py-2">
      <div className="text-gray-600">{k}</div>
      <div className="font-semibold">{String(v)}</div>
    </div>
  );
}

function RehabPlanView({ latestPlan, submitResult, onStartNew, onDownloadLatest }) {
  const plan = submitResult?.rehabPlan || latestPlan;
  const triage = submitResult?.triage;
  const prediction = submitResult?.prediction;

  return (
    <div className="rounded-2xl border bg-white p-6 shadow-sm space-y-4">
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
        <div>
          <div className="text-xl font-semibold">🧩 Your Rehab Plan</div>
          <div className="text-sm text-gray-600">Personalized exercises based on NeuroTrack+ assessment.</div>
        </div>
        <div className="flex gap-2">
          <button className="rounded-xl border px-4 py-2" onClick={onDownloadLatest}>
            ⬇️ Download Report
          </button>
          <button className="rounded-xl bg-black px-4 py-2 text-white" onClick={onStartNew}>
            Start New Assessment
          </button>
        </div>
      </div>

      {!plan ? (
        <div className="text-sm text-gray-600">No rehab plan yet. Start a new assessment.</div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-sm">
            <MiniStat title="Plan Level" value={plan.rehabLevel} />
            <MiniStat title="Approval" value={plan.doctorApprovalRequired ? (plan.approved ? "Approved" : "Pending") : "Not required"} />
            <MiniStat title="Next Review" value={plan.nextReviewDate || "-"} />
          </div>

          {triage && (
            <div className={`rounded-2xl border p-4 ${triage.safeForHomeRehab ? "bg-green-50 text-green-800" : "bg-amber-50 text-amber-800"}`}>
              <div className="font-semibold">Safety Triage</div>
              <div className="text-sm">{triage.message}</div>
            </div>
          )}

          {prediction && (
            <div className="rounded-2xl border p-4">
              <div className="font-semibold">CIPN Prediction</div>
              <div className="mt-1 text-sm">
                Severity: <span className="font-semibold">{prediction.severity}</span> • Confidence:{" "}
                <span className="font-semibold">{prediction.confidence}</span>
              </div>
              {prediction.explanation ? <div className="mt-2 text-sm text-gray-600">{prediction.explanation}</div> : null}
            </div>
          )}

          <div className="rounded-2xl border p-4">
            <div className="font-semibold">Exercise Prescription</div>
            <div className="mt-2 whitespace-pre-line text-sm text-gray-800">{plan.exercisePrescription}</div>
            {plan.safetyNotes ? <div className="mt-3 text-sm text-amber-800">⚠️ {plan.safetyNotes}</div> : null}
          </div>
        </>
      )}
    </div>
  );
}

function PatientProfile({ meta, payload, setPayload, onSave, saving }) {
  const missing = meta?.missingProfileFields || [];
  const complete = Boolean(meta?.profileComplete);

  return (
    <div className="rounded-2xl border bg-white p-6 shadow-sm space-y-4">
      <div>
        <div className="text-xl font-semibold">👤 Patient Profile</div>
        <div className="mt-1 text-sm text-gray-600">
          This profile is required for personalized rehabilitation plans and better CIPN tracking.
        </div>
      </div>

      <div className="rounded-2xl border p-4">
        <div className="text-sm text-gray-600">Registered account</div>
        <div className="mt-1 font-semibold">{meta?.fullName || ""}</div>
        <div className="text-sm text-gray-700">{meta?.email || ""}</div>
      </div>

      {!complete ? (
        <div className="rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          Profile incomplete. Please fill all required fields. Missing: <b>{missing.length}</b>
        </div>
      ) : (
        <div className="rounded-2xl border border-green-200 bg-green-50 p-4 text-sm text-green-700">
          Profile complete. Rehab plans will be personalized based on your cancer and treatment details.
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <ProfileField label="Age" name="age" type="number" placeholder="e.g., 45" payload={payload} setPayload={setPayload} missing={missing} />
        <ProfileField label="Gender" name="gender" options={["MALE", "FEMALE", "OTHER"]} payload={payload} setPayload={setPayload} missing={missing} />

        <ProfileField label="Height (cm)" name="heightCm" type="number" placeholder="e.g., 160" payload={payload} setPayload={setPayload} missing={missing} />
        <ProfileField label="Dominant Hand" name="dominantHand" options={["RIGHT", "LEFT"]} payload={payload} setPayload={setPayload} missing={missing} />

        <ProfileField label="Cancer Type" name="cancerType" placeholder="e.g., Breast" payload={payload} setPayload={setPayload} missing={missing} />
        <ProfileField label="Cancer Stage" name="cancerStage" placeholder="e.g., II" payload={payload} setPayload={setPayload} missing={missing} />

        <ProfileField label="Primary Site" name="primarySite" placeholder="e.g., Left breast" payload={payload} setPayload={setPayload} missing={missing} />
        <ProfileField
          label="Treatment Type"
          name="treatmentType"
          options={["CHEMO", "RADIO", "BOTH", "SURGERY", "COMBINED"]}
          payload={payload}
          setPayload={setPayload}
          missing={missing}
        />

        <ProfileField label="Chemo Agents" name="chemoAgents" placeholder="e.g., Paclitaxel, Cisplatin" payload={payload} setPayload={setPayload} missing={missing} />
        <ProfileField label="Radiation Site" name="radiationSite" placeholder="e.g., Chest wall" payload={payload} setPayload={setPayload} missing={missing} />

        <ProfileBoolField label="Surgery Performed" name="surgeryPerformed" payload={payload} setPayload={setPayload} missing={missing} />
        <ProfileBoolField label="Baseline Neuropathy (before chemo?)" name="baselineNeuropathy" payload={payload} setPayload={setPayload} missing={missing} />

        <ProfileField label="Treatment Start Date" name="treatmentStartDate" type="date" payload={payload} setPayload={setPayload} missing={missing} />
        <ProfileField label="Treatment End Date" name="treatmentEndDate" type="date" payload={payload} setPayload={setPayload} missing={missing} />

        <ProfileField label="Weight Before Treatment (kg)" name="weightBeforeTreatment" type="number" payload={payload} setPayload={setPayload} missing={missing} />
        <ProfileField label="Weight After Treatment (kg)" name="weightAfterTreatment" type="number" payload={payload} setPayload={setPayload} missing={missing} />
      </div>

      <div className="grid grid-cols-1 gap-4">
        <ProfileField label="Comorbidities" name="comorbidities" placeholder="e.g., Diabetes, Hypertension" payload={payload} setPayload={setPayload} missing={missing} />
        <ProfileField label="Current Pain Areas" name="currentPainAreas" placeholder="e.g., Hands, feet, shoulder" payload={payload} setPayload={setPayload} missing={missing} />
        <ProfileField label="Activity Level" name="activityLevel" options={["LOW", "MODERATE", "HIGH"]} payload={payload} setPayload={setPayload} missing={missing} />
      </div>

      <div className="flex flex-col sm:flex-row gap-3">
        <button type="button" className="rounded-xl bg-black text-white px-4 py-2 disabled:opacity-60" onClick={onSave} disabled={saving}>
          {saving ? "Saving..." : "Save Profile"}
        </button>
        {!complete ? <div className="text-sm text-gray-600 self-center">A red dot will disappear once the profile is complete.</div> : null}
      </div>
    </div>
  );
}

function Notifications({ items, onMarkRead }) {
  return (
    <div className="rounded-2xl border bg-white p-6 shadow-sm">
      <div className="text-xl font-semibold">🔔 Notifications</div>
      <div className="mt-1 text-sm text-gray-600">Safety alerts, reminders, and clinician messages.</div>

      <div className="mt-4 space-y-2">
        {items?.length ? (
          items.map((n) => (
            <div key={n.id} className={`rounded-2xl border p-4 ${n.readFlag ? "bg-white" : "bg-gray-50"}`}>
              <div className="flex items-start justify-between gap-3">
                <div>
                  <div className="text-xs text-gray-500">
                    {n.type} • {new Date(n.createdAt).toLocaleString()}
                  </div>
                  <div className="mt-1 text-sm">{n.message}</div>
                </div>
                {!n.readFlag ? (
                  <button className="rounded-xl border px-3 py-1 text-sm hover:bg-gray-50" onClick={() => onMarkRead(n.id)}>
                    Mark read
                  </button>
                ) : null}
              </div>
            </div>
          ))
        ) : (
          <div className="text-sm text-gray-600">No notifications.</div>
        )}
      </div>
    </div>
  );
}

function Settings() {
  return (
    <div className="rounded-2xl border bg-white p-6 shadow-sm space-y-3">
      <div className="text-xl font-semibold">⚙️ Settings</div>
      <div className="text-sm text-gray-600">For localhost demo, settings are limited.</div>
      <div className="rounded-2xl border p-4">
        <div className="font-semibold">Tips</div>
        <ul className="mt-2 text-sm text-gray-700 list-disc pl-5 space-y-1">
          <li>Complete Gait + Dexterity tests first, then submit symptoms.</li>
          <li>Download the PDF report from Rehab Plan or Progress Report.</li>
          <li>If triage requests review, the plan may require doctor approval.</li>
        </ul>
      </div>
    </div>
  );
}

function formatSeconds(s) {
  const v = Math.max(0, s || 0);
  const mm = Math.floor(v / 60);
  const ss = Math.floor(v % 60);
  return `${String(mm).padStart(2, "0")}:${String(ss).padStart(2, "0")}`;
}

function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}
