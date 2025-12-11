package ecotrack.backend.models.entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "carbon_record")
public class CarbonRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double emisiones_calculadas;
    private String fuente;

    @ManyToOne
    @JoinColumn(name = "activity_id")
    private DailyActivity activity;
}