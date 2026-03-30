import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api";

export default function Register() {
  const nav = useNavigate();
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("PATIENT");
  const [err, setErr] = useState(null);
  const [ok, setOk] = useState(null);

  async function onSubmit(e) {
    e.preventDefault();
    setErr(null); setOk(null);
    try {
      await api.register({ fullName, email, password, role });
      setOk("Registered. Now login.");
      setTimeout(()=>nav("/login"), 700);
    } catch (e) {
      setErr(e.message);
    }
  }

  return (
    <div className="max-w-md mx-auto rounded-2xl border bg-white p-6 shadow-sm">
      <h2 className="text-xl font-semibold">Register</h2>
      <form className="mt-4 space-y-3" onSubmit={onSubmit}>
        <div>
          <label className="text-sm text-gray-600">Full name</label>
          <input className="mt-1 w-full rounded-xl border px-3 py-2" value={fullName} onChange={e=>setFullName(e.target.value)} />
        </div>
        <div>
          <label className="text-sm text-gray-600">Email</label>
          <input className="mt-1 w-full rounded-xl border px-3 py-2" value={email} onChange={e=>setEmail(e.target.value)} />
        </div>
        <div>
          <label className="text-sm text-gray-600">Password</label>
          <input type="password" className="mt-1 w-full rounded-xl border px-3 py-2" value={password} onChange={e=>setPassword(e.target.value)} />
        </div>
        <div>
          <label className="text-sm text-gray-600">Role</label>
          <select className="mt-1 w-full rounded-xl border px-3 py-2" value={role} onChange={e=>setRole(e.target.value)}>
            <option value="PATIENT">PATIENT</option>
            <option value="DOCTOR">DOCTOR</option>
            <option value="ADMIN">ADMIN</option>
          </select>
        </div>
        {err && <div className="text-sm text-red-600">{err}</div>}
        {ok && <div className="text-sm text-green-700">{ok}</div>}
        <button className="w-full rounded-xl bg-black px-4 py-2 text-white">Create account</button>
      </form>
    </div>
  );
}
