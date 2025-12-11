// services/activityService.js
const BASE_URL = "http://localhost:8080/api/activities";

// -------------------------
// 1. Registrar actividad
// -------------------------
export async function addActivity(activityData) {
  console.log(
    "📤 Enviando actividad al backend:",
    JSON.stringify(activityData, null, 2)
  );

  try {
    const response = await fetch(`${BASE_URL}/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(activityData),
    });

    if (!response.ok) {
      const text = await response.text();
      console.error("❌ Error del backend:", text);
      throw new Error(`Error ${response.status}: ${text}`);
    }

    const result = await response.json();
    console.log("✅ Actividad registrada:", result);
    return result;
  } catch (error) {
    console.error("🔥 Error al enviar la actividad:", error);
    throw error;
  }
}

// -------------------------
// 2. Totales acumulados
// -------------------------
export async function fetchTotals(userId) {
  try {
    const response = await fetch(`${BASE_URL}/totals?userId=${userId}`);

    if (!response.ok) {
      const text = await response.text();
      throw new Error(`Error ${response.status}: ${text}`);
    }

    return await response.json();
  } catch (err) {
    console.error("❌ Error en fetchTotals:", err);
    throw err;
  }
}

// -------------------------
// 3. Historial completo
// -------------------------
export async function getHistory(userId) {
  try {
    const response = await fetch(`${BASE_URL}/history?userId=${userId}`);

    if (!response.ok) {
      const text = await response.text();
      throw new Error(`Error ${response.status}: ${text}`);
    }

    return await response.json();
  } catch (err) {
    console.error("❌ Error al cargar historial:", err);
    throw err;
  }
}

// Alias para compatibilidad
export async function fetchHistory(userId) {
  return await getHistory(userId);
}

// -------------------------
// 4. Actividades por día
// -------------------------
export async function getDailyActivities(userId, fecha) {
  try {
    const response = await fetch(
      `${BASE_URL}/daily?userId=${userId}&fecha=${fecha}`
    );

    if (!response.ok) {
      const text = await response.text();
      throw new Error(`Error ${response.status}: ${text}`);
    }

    return await response.json();
  } catch (err) {
    console.error("❌ Error cargando actividades del día:", err);
    throw err;
  }
}

// -------------------------
// 5. Actividades por rango
// -------------------------
export async function getActivitiesByRange(userId, startDate, endDate) {
  try {
    const response = await fetch(
      `${BASE_URL}/range?userId=${userId}&startDate=${startDate}&endDate=${endDate}`
    );

    if (!response.ok) {
      const text = await response.text();
      throw new Error(`Error ${response.status}: ${text}`);
    }

    return await response.json();
  } catch (err) {
    console.error("❌ Error cargando actividades por rango:", err);
    throw err;
  }
}
