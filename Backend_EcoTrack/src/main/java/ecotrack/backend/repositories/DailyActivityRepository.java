package ecotrack.backend.repositories;

import ecotrack.backend.models.entitys.DailyActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyActivityRepository extends JpaRepository<DailyActivity, Long> {
    
    // Debe retornar List, no Optional
    List<DailyActivity> findByUserIdAndFecha(Long userId, String fecha);
    
    List<DailyActivity> findByUserIdOrderByFechaDesc(Long userId);
    
    List<DailyActivity> findByUserIdAndFechaBetween(Long userId, String startDate, String endDate);
    
    Long countByUserId(Long userId);
}