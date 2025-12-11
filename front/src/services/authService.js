// servicio para register/login y manejo de storage
const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export async function registerUser({ nombre, email, password }) {
  const res = await fetch(`${BASE_URL}/api/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ nombre, email, password }),
  });

  const data = await res.json();

  if (!res.ok) {
    throw new Error(data?.message || "Error en el registro");
  }

  return data; // respuesta: { userId, nombre, email, message }
}

export function saveAuthToStorage(authObj) {
  localStorage.setItem("ecotrack_auth", JSON.stringify(authObj));
}

export function getAuthFromStorage() {
  const s = localStorage.getItem("ecotrack_auth");
  return s ? JSON.parse(s) : null;
}

export function clearAuth() {
  localStorage.removeItem("ecotrack_auth");
}
