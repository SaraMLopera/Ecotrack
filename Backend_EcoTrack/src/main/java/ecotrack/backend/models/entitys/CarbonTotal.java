package ecotrack.backend.models.entitys;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "carbon_total")
public class CarbonTotal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double total_historico;
    private Double total_mensual;
    private Double total_semanal;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
