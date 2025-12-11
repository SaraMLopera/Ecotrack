package ecotrack.backend.dto;

import lombok.Data;

@Data
public class RegisterActivityRequest {
    private Long userId;
    private String fecha;
    private String tipoActividad;
    private String descripcion;
    
    // Campos para Climatiq
    private String activityId;
    private String region;
    private String poactividad;  // Categoría de actividad (ej: "Transporte - Automóvil")
    
    // Parámetros de TRANSPORTE
    private String transportation_distance;
    private String transportation_unit;
    
    // Parámetros de COMBUSTIBLE
    private String fuel_source_volume;
    private String fuel_source_unit;
    
    // Parámetros de ELECTRICIDAD
    private String electricity_value;
    private String electricity_unit;
    
    // Parámetros de RESIDUOS (si los necesitas después)
    private String waste_weight;
    private String waste_unit;
    
    // Parámetros de AGUA (si los necesitas después)
    private String water_volume;
    private String water_unit;
}