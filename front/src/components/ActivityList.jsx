import styles from "./ActivityList.module.css";

export default function ActivityList({ activities }) {
  if (!activities || activities.length === 0)
    return <p>No has registrado actividades aún.</p>;

  return (
    <div className={styles.activityList}>
      <h3>Historial</h3>
      <ul>
        {activities.map((a) => (
          <li key={a.id} className={styles.activityItem}>
            <strong>{a.fecha}</strong> — {a.tipoActividad}
            <br />
            {a.descripcion}
            <br />
            <span>{a.emisiones} kg CO₂</span>
          </li>
        ))}
      </ul>
    </div>
  );
}
