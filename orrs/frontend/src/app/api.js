const API_BASE = "http://localhost:8080/api";

export function getToken() {
  return localStorage.getItem("token");
}
export function setToken(token) {
  localStorage.setItem("token", token);
}
export function clearToken() {
  localStorage.removeItem("token");
  localStorage.removeItem("role");
  localStorage.removeItem("fullName");
}

async function request(path, { method = "GET", body } = {}) {
  const headers = { "Content-Type": "application/json" };
  const token = getToken();
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) {
    const msg = (data && (data.message || data.error)) || `HTTP ${res.status}`;
    throw new Error(msg);
  }
  return data;
}

export const api = {
  register: (payload) => request("/auth/register", { method: "POST", body: payload }),
  login: (payload) => request("/auth/login", { method: "POST", body: payload }),
  me: () => request("/patient/me"),
  patientDashboard: () => request("/patient/dashboard"),
  getPatientProfile: () => request("/patient/profile"),
  updatePatientProfile: (payload) => request("/patient/profile", { method: "PUT", body: payload }),
  patientSessions: () => request("/patient/sessions"),
  patientSessionDetails: (sessionId) => request(`/patient/sessions/${sessionId}`),
  patientNotifications: () => request("/patient/notifications"),
  markNotificationRead: (id) => request(`/patient/notifications/${id}/read`, { method: "PUT" }),
  submitAssessment: (payload) => request("/patient/assessment", { method: "POST", body: payload }),
  downloadPatientReport: async (sessionId) => {
    const token = getToken();
    const res = await fetch(`${API_BASE}/patient/report/pdf${sessionId ? `?sessionId=${sessionId}` : ""}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const blob = await res.blob();
    return blob;
  },
  doctorPatients: () => request("/doctor/patients"),
  doctorSessions: (patientId) => request(`/doctor/patients/${patientId}/sessions`),
  doctorSessionDetails: (patientId, sessionId) => request(`/doctor/patients/${patientId}/sessions/${sessionId}`),
  doctorNotes: (patientId) => request(`/doctor/patients/${patientId}/notes`),
  addDoctorNote: (patientId, payload) => request(`/doctor/patients/${patientId}/notes`, { method: "POST", body: payload }),
  updateRehabPlan: (planId, payload) => request(`/doctor/rehab-plans/${planId}`, { method: "PUT", body: payload }),
  downloadDoctorReport: async (patientId, sessionId) => {
    const token = getToken();
    const res = await fetch(`${API_BASE}/doctor/patients/${patientId}/report/pdf?sessionId=${sessionId}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.blob();
  },
};
