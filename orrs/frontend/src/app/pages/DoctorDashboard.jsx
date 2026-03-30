import React, { useEffect, useMemo, useState } from "react";
import { api } from "../api";

export default function DoctorDashboard() {
  const [patients, setPatients] = useState([]);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [sessions, setSessions] = useState([]);
  const [selectedSessionId, setSelectedSessionId] = useState(null);
  const [sessionDetails, setSessionDetails] = useState(null);
  const [notes, setNotes] = useState([]);
  const [newNote, setNewNote] = useState("");
  const [planDraft, setPlanDraft] = useState({ exercisePrescription: "", safetyNotes: "", approved: false });
  const [err, setErr] = useState(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        setPatients(await api.doctorPatients());
      } catch (e) {
        setErr(e.message);
      }
    })();
  }, []);

  useEffect(() => {
    (async () => {
      if (!selectedPatient) return;
      setSelectedSessionId(null);
      setSessionDetails(null);
      try {
        setSessions(await api.doctorSessions(selectedPatient.id));
        setNotes(await api.doctorNotes(selectedPatient.id));
      } catch (e) {
        setErr(e.message);
      }
    })();
  }, [selectedPatient]);

  useEffect(() => {
    (async () => {
      if (!selectedSessionId || !selectedPatient) return;
      try {
        setErr(null);
        const d = await api.doctorSessionDetails(selectedPatient.id, selectedSessionId);
        setSessionDetails(d);
      } catch (e) {
        setErr(e.message);
      }
    })();
  }, [selectedSessionId, selectedPatient]);

  const selectedSession = useMemo(
    () => sessions.find((s) => s.id === selectedSessionId) || null,
    [sessions, selectedSessionId]
  );

  async function downloadPdf() {
    if (!selectedPatient || !selectedSessionId) return;
    try {
      const blob = await api.downloadDoctorReport(selectedPatient.id, selectedSessionId);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `NeuroTrack_Patient_${selectedPatient.id}_Session_${selectedSessionId}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch (e) {
      setErr(e.message);
    }
  }

  async function addNote() {
    if (!selectedPatient) return;
    const note = newNote.trim();
    if (!note) return;
    setBusy(true);
    setErr(null);
    try {
      await api.addDoctorNote(selectedPatient.id, { note, sessionId: selectedSessionId || null });
      setNewNote("");
      setNotes(await api.doctorNotes(selectedPatient.id));
    } catch (e) {
      setErr(e.message);
    } finally {
      setBusy(false);
    }
  }

  async function updatePlan() {
    // Plan editing depends on planId; we expect doctors to copy planId from the Patient PDF (Phase-3 reality).
    // To keep the UI useful, we allow entering planId in the UI.
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
      <div className="rounded-2xl border bg-white p-5 shadow-sm">
        <h2 className="text-xl font-semibold">Doctor Panel</h2>
        <p className="mt-1 text-sm text-gray-600">Select a patient to review sessions, download reports, and add notes.</p>

        {err && <div className="mt-3 text-sm text-red-600">{err}</div>}

        <div className="mt-4">
          <div className="text-sm font-medium text-gray-700">Patients</div>
          <div className="mt-2 space-y-2 max-h-[60vh] overflow-auto pr-1">
            {patients.map((p) => (
              <button
                key={p.id}
                className={`w-full text-left rounded-xl border px-3 py-2 hover:bg-gray-50 ${selectedPatient?.id === p.id ? "bg-gray-50" : ""}`}
                onClick={() => setSelectedPatient(p)}
              >
                <div className="font-medium">{p.user?.fullName || `Patient #${p.id}`}</div>
                <div className="text-xs text-gray-600">Profile ID: {p.id}</div>
              </button>
            ))}
            {patients.length === 0 && <div className="text-sm text-gray-500">No patients yet.</div>}
          </div>
        </div>
      </div>

      <div className="rounded-2xl border bg-white p-5 shadow-sm lg:col-span-2">
        {!selectedPatient ? (
          <EmptyState />
        ) : (
          <>
            <div className="flex items-start justify-between gap-3">
              <div>
                <div className="text-sm text-gray-500">Selected patient</div>
                <div className="text-xl font-semibold">{selectedPatient.user?.fullName || `Patient #${selectedPatient.id}`}</div>
              </div>
            </div>

            <div className="mt-4 grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <div className="flex items-center justify-between">
                  <div className="text-sm font-medium text-gray-700">Assessment Sessions</div>
                  <button
                    className={`rounded-lg border px-3 py-1 text-sm hover:bg-gray-50 ${!selectedSessionId ? "opacity-50" : ""}`}
                    disabled={!selectedSessionId}
                    onClick={downloadPdf}
                  >
                    Download PDF
                  </button>
                </div>
                <div className="mt-2 rounded-xl border overflow-hidden">
                  <table className="w-full text-sm">
                    <thead className="bg-gray-50 text-gray-600">
                      <tr>
                        <th className="text-left px-3 py-2">Session</th>
                        <th className="text-left px-3 py-2">Date</th>
                      </tr>
                    </thead>
                    <tbody>
                      {sessions.map((s) => (
                        <tr
                          key={s.id}
                          className={`cursor-pointer hover:bg-gray-50 ${selectedSessionId === s.id ? "bg-gray-50" : ""}`}
                          onClick={() => setSelectedSessionId(s.id)}
                        >
                          <td className="px-3 py-2 font-medium">#{s.id}</td>
                          <td className="px-3 py-2">{new Date(s.assessmentDate).toLocaleString()}</td>
                        </tr>
                      ))}
                      {sessions.length === 0 && (
                        <tr>
                          <td className="px-3 py-4 text-gray-500" colSpan={2}>
                            No sessions for this patient.
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
                <div className="mt-2 text-xs text-gray-500">
                  Tip: Select a session then download the PDF report to see symptoms, tests, prediction and rehab plan.
                </div>
              </div>

              <div>
                <div className="text-sm font-medium text-gray-700">Doctor Notes</div>
                <div className="mt-2 space-y-2 max-h-[22rem] overflow-auto pr-1">
                  {notes.map((n) => (
                    <div key={n.id} className="rounded-xl border p-3">
                      <div className="text-xs text-gray-500">
                        {new Date(n.createdAt).toLocaleString()} • Session {n.session?.id ?? "—"}
                      </div>
                      <div className="mt-1">{n.note}</div>
                    </div>
                  ))}
                  {notes.length === 0 && <div className="text-sm text-gray-500">No notes yet.</div>}
                </div>

                <div className="mt-3">
                  <textarea
                    className="w-full rounded-xl border px-3 py-2 text-sm"
                    rows={3}
                    placeholder="Add a clinical note / suggestion..."
                    value={newNote}
                    onChange={(e) => setNewNote(e.target.value)}
                  />
                  <div className="mt-2 flex items-center gap-2">
                    <button
                      className="rounded-xl bg-black px-4 py-2 text-white text-sm disabled:opacity-50"
                      disabled={busy || !newNote.trim()}
                      onClick={addNote}
                    >
                      Save Note
                    </button>
                    {selectedSession && (
                      <span className="text-xs text-gray-500">Will attach to session #{selectedSession.id}</span>
                    )}
                  </div>
                </div>
              </div>
            </div>

            <div className="mt-6 rounded-2xl border p-4 bg-gray-50">
              <div className="text-sm font-medium">Rehab Plan Updates (optional)</div>
              <p className="mt-1 text-sm text-gray-600">
                For Phase-3, rehab plans are generated automatically by ORRS. Doctors can update/approve plans if you add the plan ID.
              </p>
              <PlanUpdater setErr={setErr} />
            </div>
          </>
        )}
      </div>
    </div>
  );
}

function EmptyState() {
  return (
    <div className="rounded-2xl border bg-white p-10 text-center">
      <div className="text-xl font-semibold">Select a patient</div>
      <div className="mt-2 text-sm text-gray-600">You’ll be able to review sessions, download reports, and add notes.</div>
    </div>
  );
}

function PlanUpdater({ setErr }) {
  const [planId, setPlanId] = useState("");
  const [exercisePrescription, setExercisePrescription] = useState("");
  const [safetyNotes, setSafetyNotes] = useState("");
  const [approved, setApproved] = useState(false);
  const [busy, setBusy] = useState(false);

  async function save() {
    const id = Number(planId);
    if (!id) {
      setErr("Enter a valid planId");
      return;
    }
    setBusy(true);
    try {
      await api.updateRehabPlan(id, { exercisePrescription, safetyNotes, approved });
      setErr(null);
      alert("Plan updated");
    } catch (e) {
      setErr(e.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="mt-3 grid grid-cols-1 md:grid-cols-2 gap-3">
      <Field label="Plan ID" value={planId} onChange={setPlanId} placeholder="e.g., 12" />
      <div className="flex items-center gap-2 mt-6">
        <input type="checkbox" checked={approved} onChange={(e) => setApproved(e.target.checked)} />
        <span className="text-sm">Approve plan</span>
      </div>
      <div className="md:col-span-2">
        <label className="text-sm text-gray-600">Exercise Prescription</label>
        <textarea className="mt-1 w-full rounded-xl border px-3 py-2 text-sm" rows={4} value={exercisePrescription} onChange={(e) => setExercisePrescription(e.target.value)} />
      </div>
      <div className="md:col-span-2">
        <label className="text-sm text-gray-600">Safety Notes</label>
        <textarea className="mt-1 w-full rounded-xl border px-3 py-2 text-sm" rows={2} value={safetyNotes} onChange={(e) => setSafetyNotes(e.target.value)} />
      </div>
      <div className="md:col-span-2">
        <button className="rounded-xl bg-black px-4 py-2 text-white text-sm disabled:opacity-50" disabled={busy} onClick={save}>
          Save plan
        </button>
      </div>
    </div>
  );
}

function Field({ label, value, onChange, placeholder }) {
  return (
    <div>
      <label className="text-sm text-gray-600">{label}</label>
      <input className="mt-1 w-full rounded-xl border px-3 py-2 text-sm" value={value} placeholder={placeholder} onChange={(e) => onChange(e.target.value)} />
    </div>
  );
}
