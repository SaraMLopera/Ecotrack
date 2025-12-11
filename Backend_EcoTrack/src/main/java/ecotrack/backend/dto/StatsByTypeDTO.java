package ecotrack.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsByTypeDTO {
    private Long userId;
    private Map<String, Double> emisionesPorTipo; // {"Electricidad": 150.5, "Transporte": 80.2}
    private Map<String, Integer> actividadesPorTipo; // {"Electricidad": 5, "Transporte": 3}
    private String tipoMasContaminante;
    private Double totalEmisiones;
}