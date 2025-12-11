import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { registerUser, saveAuthToStorage } from "../services/authService";
import styles from "./Register.module.css";

export default function Register() {
  const [nombre, setNombre] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);

    if (!nombre.trim() || !email.trim() || password.length < 6) {
      setError(
        "Completa todos los campos. La contraseña debe tener al menos 6 caracteres."
      );
      return;
    }

    setLoading(true);
    try {
      const authResponse = await registerUser({ nombre, email, password });
      saveAuthToStorage(authResponse);
      navigate("/home");
    } catch (err) {
      setError(err.message || "Error durante el registro");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className={styles.registerContainer}>
      <div className={styles.registerBox}>
        <h2 className={styles.title}>Registro</h2>
        <form onSubmit={handleSubmit} className={styles.form}>
          <label className={styles.label}>Nombre</label>
          <input
            className={styles.input}
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
            placeholder="Tu nombre"
          />

          <label className={styles.label}>Email</label>
          <input
            className={styles.input}
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="correo@ejemplo.com"
            type="email"
          />

          <label className={styles.label}>Contraseña</label>
          <input
            className={styles.input}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="mínimo 6 caracteres"
            type="password"
          />

          {error && <div className={styles.error}>{error}</div>}

          <button type="submit" disabled={loading} className={styles.button}>
            {loading ? "Registrando..." : "Registrarme"}
          </button>
        </form>

        <p className={styles.linkText}>
          ¿Ya tienes cuenta? <Link to="/Login">Iniciar sesión</Link>
        </p>
      </div>
    </div>
  );
}
