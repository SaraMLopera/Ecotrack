import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { saveAuthToStorage } from "../services/authService";
import styles from "./Login.module.css";
import logo from "../assets/EcoTrack.png";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    try {
      const res = await fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });

      const data = await res.json();

      if (!res.ok) {
        setError(data.message || "Error en servidor");
        return;
      }

      if (data.message === "Login exitoso") {
        const authData = {
          userId: data.userId,
          nombre: data.nombre,
          email: data.email,
        };

        saveAuthToStorage(authData);
        setSuccess(`Bienvenida ${data.nombre}`);
        navigate("/Home");
      } else {
        setError("Credenciales incorrectas");
      }
    } catch (err) {
      setError("Error conectando con el backend");
      console.error(err);
    }
  };

  return (
    <div className={styles.loginContainer}>
      <div className={styles.loginBox}>
        <img src={logo} alt="EcoTrack Logo" className={styles.logo} />
        <h1 className={styles.title}>Iniciar Sesión</h1>
        <form onSubmit={handleLogin} className={styles.form}>
          <input
            type="email"
            placeholder="Correo electrónico"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className={styles.input}
            required
          />
          <input
            type="password"
            placeholder="Contraseña"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className={styles.input}
            required
          />
          <button type="submit" className={styles.button}>
            Entrar
          </button>
        </form>
        {error && <p className={styles.error}>{error}</p>}
        {success && <p className={styles.success}>{success}</p>}
        <Link to="/register" className={styles.link}>
          ¿No tienes cuenta? Regístrate
        </Link>
      </div>
    </div>
  );
}
