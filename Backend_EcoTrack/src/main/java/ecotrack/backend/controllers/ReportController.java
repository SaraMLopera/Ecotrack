package ecotrack.backend.controllers;

import ecotrack.backend.dto.ActivityHistoryDTO;
import ecotrack.backend.dto.StatsByTypeDTO;
import ecotrack.backend.dto.TotalsReportDTO;
import ecotrack.backend.services.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Obtener todas las actividades de un usuario
     * GET /api/reports/activities/{userId}
     */
    @GetMapping("/activities/{userId}")
    public ResponseEntity<List<ActivityHistoryDTO>> getUserActivities(@PathVariable Long userId) {
        List<ActivityHistoryDTO> activities = reportService.getUserActivities(userId);
        return ResponseEntity.ok(activities);
    }

    /**
     * Obtener actividades por rango de fechas
     * GET /api/reports/activities/{userId}/range?start=2025-12-01&end=2025-12-06
     */
    @GetMapping("/activities/{userId}/range")
    public ResponseEntity<List<ActivityHistoryDTO>> getUserActivitiesByRange(
            @PathVariable Long userId,
            @RequestParam String start,
            @RequestParam String end) {
        
        List<ActivityHistoryDTO> activities = reportService
                .getUserActivitiesByDateRange(userId, start, end);
        return ResponseEntity.ok(activities);
    }

    /**
     * Obtener totales de carbono del usuario
     * GET /api/reports/totals/{userId}
     */
    @GetMapping("/totals/{userId}")
    public ResponseEntity<TotalsReportDTO> getUserTotals(@PathVariable Long userId) {
        TotalsReportDTO totals = reportService.getUserTotals(userId);
        return ResponseEntity.ok(totals);
    }

    /**
     * Obtener estadísticas por tipo de actividad
     * GET /api/reports/stats/{userId}
     */
    @GetMapping("/stats/{userId}")
    public ResponseEntity<StatsByTypeDTO> getUserStatsByType(@PathVariable Long userId) {
        StatsByTypeDTO stats = reportService.getUserStatsByType(userId);
        return ResponseEntity.ok(stats);
    }
}
