package ecotrack.backend.repositories;

import ecotrack.backend.models.entitys.CarbonRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarbonRecordRepository extends JpaRepository<CarbonRecord, Long> {
    
    // CORRECCIÓN: usar la ruta completa de relaciones
    // CarbonRecord -> activity -> user -> id
    List<CarbonRecord> findByActivityUserId(Long userId);
    
    // Buscar por lista de IDs de actividades
    @Query("SELECT r FROM CarbonRecord r WHERE r.activity.id IN :activityIds")
    List<CarbonRecord> findByActivityIdIn(@Param("activityIds") List<Long> activityIds);
    
    // Sumar emisiones totales de un usuario
    @Query("SELECT SUM(r.emisiones_calculadas) FROM CarbonRecord r " +
           "WHERE r.activity.user.id = :userId")
    Double sumEmissionsByUser(@Param("userId") Long userId);
    
    // Sumar emisiones por rango de fechas
    @Query("SELECT SUM(r.emisiones_calculadas) FROM CarbonRecord r " +
           "WHERE r.activity.user.id = :userId " +
           "AND r.activity.fecha BETWEEN :startDate AND :endDate")
    Double sumEmissionsByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}