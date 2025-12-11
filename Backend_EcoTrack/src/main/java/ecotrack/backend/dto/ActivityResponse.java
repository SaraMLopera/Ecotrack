package ecotrack.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {
    private Long activityId;
    private String fecha;
    private String tipoActividad;
    private String descripcion;
    private String poactividad;  // Categoría de actividad
    private String climatiqActivityId;  // El activity_id de Climatiq usado
    private String region;
    private Double co2e;  // kg CO2e calculados
    private String fuente;  // "climatiq"
    
    // Detalles de los parámetros usados (opcional, para debugging)
    private String parametersUsed;  // JSON string de los parámetros enviados a Climatiq
}