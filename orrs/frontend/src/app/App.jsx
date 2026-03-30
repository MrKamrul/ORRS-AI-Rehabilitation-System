import React from "react";
import { Routes, Route, Navigate, Link, useNavigate } from "react-router-dom";
import Login from "./pages/Login";
import Register from "./pages/Register";
import PatientDashboard from "./pages/PatientDashboard";
import DoctorDashboard from "./pages/DoctorDashboard";
import { clearToken, getToken } from "./api";

function TopBar() {
  const nav = useNavigate();
  const token = getToken();
  const role = localStorage.getItem("role");
  const fullName = localStorage.getItem("fullName");

  return (
    <div className="w-full border-b bg-white">
      <div className="mx-auto max-w-5xl px-4 py-3 flex items-center justify-between">
        <Link to="/" className="font-semibold">ORRS</Link>
        <div className="flex items-center gap-3 text-sm">
          {token ? (
            <>
              <span className="text-gray-600">{fullName} • {role}</span>
              <button
                className="rounded-lg border px-3 py-1 hover:bg-gray-50"
                onClick={() => { clearToken(); nav("/login"); }}
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link className="hover:underline" to="/login">Login</Link>
              <Link className="hover:underline" to="/register">Register</Link>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

function RequireAuth({ children, role }) {
  const token = getToken();
  const r = localStorage.getItem("role");
  if (!token) return <Navigate to="/login" replace />;
  if (role && r !== role) return <Navigate to="/" replace />;
  return children;
}

export default function App() {
  return (
    <div className="min-h-screen bg-gray-50">
      <TopBar />
      <div className="mx-auto max-w-5xl px-4 py-6">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          <Route path="/patient" element={
            <RequireAuth role="PATIENT"><PatientDashboard /></RequireAuth>
          }/>

          <Route path="/doctor" element={
            <RequireAuth role="DOCTOR"><DoctorDashboard /></RequireAuth>
          }/>
        </Routes>
      </div>
    </div>
  );
}

function Home() {
  const role = localStorage.getItem("role");
  if (role === "PATIENT") return <Navigate to="/patient" replace />;
  if (role === "DOCTOR") return <Navigate to="/doctor" replace />;

  return (
    <div className="rounded-2xl border bg-white p-6 shadow-sm">
      <h1 className="text-2xl font-semibold">Onco-Rehabilitation Specialist System (ORRS)</h1>
      <p className="mt-2 text-gray-600">
        Phase-3 demo: secure login, patient assessment (CIPN + gait + dexterity), safety triage, prediction, and rehab plan generation.
      </p>
      <div className="mt-4 flex gap-3">
        <Link className="rounded-xl bg-black px-4 py-2 text-white" to="/login">Login</Link>
        <Link className="rounded-xl border px-4 py-2" to="/register">Register</Link>
      </div>
    </div>
  );
}
