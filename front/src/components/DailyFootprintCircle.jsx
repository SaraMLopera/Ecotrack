import styles from "./DailyFootprintCircle.module.css";

export default function DailyFootprintCircle({ totals }) {
  if (!totals) return null;
  const { totalSemanal } = totals;

  return (
    <div className={styles.circleContainer}>
      <div className={styles.circle}>
        <p>{totalSemanal.toFixed(2)} kg CO₂</p>
      </div>
    </div>
  );
}
