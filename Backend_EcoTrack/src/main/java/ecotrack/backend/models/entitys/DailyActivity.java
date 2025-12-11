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
@Table(name = "daily_activity")
public class DailyActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descripcion;
    private String fecha;
    private String tipo_actividad;
    
    @Column(name = "activity_id")
    private String activityId;
    
    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;
    
    private String region;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}