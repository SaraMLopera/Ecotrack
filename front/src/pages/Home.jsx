import { useEffect, useState } from "react";
import { fetchTotals, fetchHistory } from "../services/activityService";

import Sidebar from "../components/Sidebar";
import DailyFootprintCircle from "../components/DailyFootprintCircle";
import AddActivityForm from "../components/AddActivityForm";
import ActivityList from "../components/ActivityList";

import styles from "./Home.module.css";

export default function Home() {
  const auth = JSON.parse(localStorage.getItem("ecotrack_auth"));
  const userId = auth?.userId;

  const [totals, setTotals] = useState(null);
  const [history, setHistory] = useState([]);
  const [sidebarOpen, setSidebarOpen] = useState(false); // controla Sidebar

  async function loadData() {
    if (!userId) return;
    const t = await fetchTotals(userId);
    const h = await fetchHistory(userId);
    setTotals(t);
    setHistory(h);
  }

  useEffect(() => {
    loadData();
  }, []);

  return (
    <div className={styles.homeLayout}>
      {/* Botón menú para abrir Sidebar */}
      <button
        onClick={() => setSidebarOpen(true)}
        className={styles.menuButton}
      >
        ☰
      </button>

      <Sidebar
        isOpen={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
        onLogout={() => {
          localStorage.removeItem("ecotrack_auth");
          window.location.href = "/";
        }}
      />

      <div className={styles.homeContent}>
        <h1 className={styles.title}>Bienvenido, {auth?.nombre} 🌱</h1>

        <DailyFootprintCircle totals={totals} />

        <AddActivityForm userId={userId} onAdded={loadData} />

        <ActivityList activities={history} />
      </div>
    </div>
  );
}
