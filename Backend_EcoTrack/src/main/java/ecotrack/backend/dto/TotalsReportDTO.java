package ecotrack.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotalsReportDTO {
    private Long userId;
    private String userName;
    private Double totalHistorico;
    private Double totalMensual;
    private Double totalSemanal;
    private Integer actividadesRegistradas;
}
