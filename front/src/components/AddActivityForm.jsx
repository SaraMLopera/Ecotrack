import { useState, useEffect } from "react";
import styles from "./AddActivityForm.module.css";
import { addActivity } from "../services/activityService";

const ACTIVITY_CONFIG = {
  Electricidad: {
    activityId: "electricity-supply_grid-source_supplier_mix",
    searchQuery: "electricity",
    paramKey: "energy",
    defaultUnit: "kWh",
    label: "Consumo eléctrico (kWh)",
  },
  "Transporte - Automóvil": {
    activityId:
      "passenger_vehicle-vehicle_type_car-fuel_source_na-engine_size_na-vehicle_age_na-vehicle_weight_na",
    searchQuery: "passenger vehicle car",
    paramKey: "distance",
    defaultUnit: "km",
    label: "Distancia recorrida (km)",
  },
  "Transporte - Avión": {
    activityId:
      "passenger_flight-route_type_na-aircraft_type_na-distance_na-class_na-rf_included",
    searchQuery: "passenger flight",
    paramKey: "distance",
    defaultUnit: "km",
    label: "Distancia de vuelo (km)",
  },
  "Transporte - Tren": {
    activityId: "passenger_train-route_type_na-fuel_source_na",
    searchQuery: "passenger train",
    paramKey: "distance",
    defaultUnit: "km",
    label: "Distancia recorrida (km)",
  },
  Combustibles: {
    activityId: "fuel_combustion_stationary-fuel_source_na",
    searchQuery: "fuel combustion",
    paramKey: "volume",
    defaultUnit: "l",
    label: "Volumen del combustible (litros)",
  },
  Materiales: {
    activityId: "material_use-type_na",
    searchQuery: "material",
    paramKey: "weight",
    defaultUnit: "kg",
    label: "Peso del material (kg)",
  },
  "Agua y Residuos": {
    activityId: "waste_water_treatment-type_na",
    searchQuery: "waste water",
    paramKey: "volume",
    defaultUnit: "l",
    label: "Volumen (litros)",
  },
};

export default function AddActivityForm({
  userId,
  onAdded,
  apiUrl = "http://localhost:8080",
  useSearch = false,
}) {
  const [form, setForm] = useState({
    fecha: "",
    tipoActividad: "",
    descripcion: "",
    activityId: "",
    region: "CO",
  });

  const [parameterValue, setParameterValue] = useState("");
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (form.tipoActividad && useSearch) {
      searchActivities(form.tipoActividad);
    }
  }, [form.tipoActividad, useSearch]);

  async function searchActivities(category) {
    const config = ACTIVITY_CONFIG[category];
    if (!config) return;

    setLoading(true);
    setError("");

    try {
      const res = await fetch(
        `${apiUrl}/api/climatiq/search?query=${encodeURIComponent(
          config.searchQuery
        )}&region=${form.region}`
      );

      if (!res.ok) throw new Error("Error en búsqueda");
      const data = await res.json();

      if (Array.isArray(data.results)) {
        setActivities(data.results);
        if (data.results.length > 0) {
          setForm((prev) => ({
            ...prev,
            activityId: data.results[0].activity_id,
          }));
        }
      }
    } catch (err) {
      setError("No se pudo conectar con el backend.");
    } finally {
      setLoading(false);
    }
  }

  function handleCategoryChange(e) {
    const category = e.target.value;
    const cfg = ACTIVITY_CONFIG[category];

    if (!cfg) return;

    setForm({
      ...form,
      tipoActividad: category,
      activityId: cfg.activityId,
    });

    setParameterValue("");
    setActivities([]);
    setError("");
  }

  async function handleSubmit(e) {
    e.preventDefault();

    if (!form.activityId) {
      alert("Por favor seleccione una categoría");
      return;
    }

    if (!parameterValue || parseFloat(parameterValue) <= 0) {
      alert("Por favor ingrese un valor válido");
      return;
    }

    const cfg = ACTIVITY_CONFIG[form.tipoActividad];
    const numericValue = parseFloat(parameterValue);

    // ✅ PAYLOAD CORRECTO PARA EL BACKEND
    const payload = {
      userId: Number(userId),
      fecha: form.fecha,
      tipoActividad: form.tipoActividad,
      descripcion: form.descripcion,
      activityId: form.activityId,
      region: form.region,
      poactividad: form.tipoActividad,
    };

    // Agregar campos específicos según el tipo
    if (cfg.paramKey === "energy") {
      payload.electricity_value = String(numericValue);
      payload.electricity_unit = cfg.defaultUnit;
    } else if (cfg.paramKey === "distance") {
      payload.transportation_distance = String(numericValue);
      payload.transportation_unit = cfg.defaultUnit;
    } else if (
      cfg.paramKey === "volume" &&
      form.tipoActividad === "Combustibles"
    ) {
      payload.fuel_source_volume = String(numericValue);
      payload.fuel_source_unit = cfg.defaultUnit;
    } else if (
      cfg.paramKey === "volume" &&
      form.tipoActividad === "Agua y Residuos"
    ) {
      payload.water_volume = String(numericValue);
      payload.water_unit = cfg.defaultUnit;
    } else if (cfg.paramKey === "weight") {
      payload.waste_weight = String(numericValue);
      payload.waste_unit = cfg.defaultUnit;
    }

    console.log("📦 Payload:", JSON.stringify(payload, null, 2));

    try {
      const result = await addActivity(payload);
      console.log("✅ Resultado:", result);
      alert("Actividad registrada ✔");

      setForm({
        fecha: "",
        tipoActividad: "",
        descripcion: "",
        activityId: "",
        region: "CO",
      });
      setParameterValue("");

      if (onAdded) onAdded();
    } catch (err) {
      console.error("❌ Error:", err);
      alert("Error al registrar la actividad: " + err.message);
    }
  }

  const cfg = form.tipoActividad ? ACTIVITY_CONFIG[form.tipoActividad] : null;

  return (
    <div className={styles.container}>
      <h3 className={styles.title}>Agregar Actividad</h3>

      <input
        name="descripcion"
        placeholder="Descripción"
        value={form.descripcion}
        onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
        required
        className={styles.input}
      />

      <input
        type="date"
        name="fecha"
        value={form.fecha}
        onChange={(e) => setForm({ ...form, fecha: e.target.value })}
        required
        className={styles.input}
      />

      <select
        value={form.tipoActividad}
        onChange={handleCategoryChange}
        required
        className={styles.select}
      >
        <option value="">Seleccione categoría</option>
        {Object.keys(ACTIVITY_CONFIG).map((cat) => (
          <option key={cat} value={cat}>
            {cat}
          </option>
        ))}
      </select>

      {loading && <p className={styles.loading}>Buscando actividades...</p>}
      {error && <p className={styles.error}>{error}</p>}

      {cfg && form.activityId && (
        <input
          type="number"
          step="0.01"
          min="0"
          value={parameterValue}
          placeholder={cfg.label}
          onChange={(e) => setParameterValue(e.target.value)}
          required
          className={styles.input}
        />
      )}

      <button
        onClick={handleSubmit}
        disabled={!form.activityId || loading || !parameterValue}
        className={styles.button}
      >
        Guardar
      </button>
    </div>
  );
}
