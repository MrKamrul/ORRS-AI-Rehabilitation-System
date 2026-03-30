import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, setToken } from "../api";

export default function Login() {
  const nav = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState(null);

  async function onSubmit(e) {
    e.preventDefault();
    setErr(null);
    try {
      const res = await api.login({ email, password });
      setToken(res.token);
      localStorage.setItem("role", res.role);
      localStorage.setItem("fullName", res.fullName || "");
      if (res.role === "PATIENT") nav("/patient");
      else if (res.role === "DOCTOR") nav("/doctor");
      else nav("/");
    } catch (e) {
      setErr(e.message);
    }
  }

  return (
    <div className="max-w-md mx-auto rounded-2xl border bg-white p-6 shadow-sm">
      <h2 className="text-xl font-semibold">Login</h2>
      <form className="mt-4 space-y-3" onSubmit={onSubmit}>
        <div>
          <label className="text-sm text-gray-600">Email</label>
          <input className="mt-1 w-full rounded-xl border px-3 py-2" value={email} onChange={e=>setEmail(e.target.value)} />
        </div>
        <div>
          <label className="text-sm text-gray-600">Password</label>
          <input type="password" className="mt-1 w-full rounded-xl border px-3 py-2" value={password} onChange={e=>setPassword(e.target.value)} />
        </div>
        {err && <div className="text-sm text-red-600">{err}</div>}
        <button className="w-full rounded-xl bg-black px-4 py-2 text-white">Sign in</button>
      </form>
      <p className="mt-3 text-sm text-gray-600">
        Tip: create a PATIENT and a DOCTOR account in Register.
      </p>
    </div>
  );
}
