import { Link } from "react-router-dom";
import styles from "./Sidebar.module.css";

export default function Sidebar({ isOpen, onClose, onLogout }) {
  return (
    <div className={`${styles.sidebar} ${isOpen ? styles.sidebarOpen : ""}`}>
      <button onClick={onClose} className={styles.closeButton}>
        ✕
      </button>
      <h2 className={styles.logo}>EcoTrack</h2>
      <nav className={styles.nav}>
        <Link to="/home" className={styles.link} onClick={onClose}>
          Inicio
        </Link>
        <Link to="/stats" className={styles.link} onClick={onClose}>
          Estadísticas
        </Link>
        <Link to="/Login" className={styles.link} onClick={onClose}>
          Cerrar sesión
        </Link>
      </nav>
    </div>
  );
}
