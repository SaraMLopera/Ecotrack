package ecotrack.backend.controllers;

import ecotrack.backend.dto.*;
import ecotrack.backend.services.ActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService service;

    public ActivityController(ActivityService service) {
        this.service = service;
    }

    // ----------------------------
    // 1) Registrar actividad
    // ----------------------------
    @PostMapping("/register")
    public ResponseEntity<ActivityResponse> register(@RequestBody RegisterActivityRequest req) {
        System.out.println("🔍 Controller recibió:");
        System.out.println("   Fecha: " + req.getFecha());
        System.out.println("   Descripción: " + req.getDescripcion());
        System.out.println("   Categoría: " + req.getPoactividad());
        System.out.println("   Activity ID: " + req.getActivityId());
        System.out.println("   Transportation Unit: " + req.getTransportation_unit());
        System.out.println("   Transportation Distance: " + req.getTransportation_distance());
        System.out.println("   Fuel Unit: " + req.getFuel_source_unit());
        System.out.println("   Fuel Volume: " + req.getFuel_source_volume());
        System.out.println("   Electricity Unit: " + req.getElectricity_unit());
        System.out.println("   Electricity Value: " + req.getElectricity_value());
        
        ActivityResponse response = service.registerActivity(req);
        
        System.out.println("✅ Actividad registrada con CO2e: " + response.getCo2e());
        
        return ResponseEntity.ok(response);
    }

    // ----------------------------
    // 2) Actividades del día (Home)
    // ----------------------------
    @GetMapping("/daily")
    public ResponseEntity<List<ActivityHistoryDTO>> getDaily(
            @RequestParam Long userId,
            @RequestParam String fecha
    ) {
        return ResponseEntity.ok(service.getDailyActivities(userId, fecha));
    }

    // ----------------------------
    // 3) Historial completo
    // ----------------------------
    @GetMapping("/history")
    public ResponseEntity<List<ActivityHistoryDTO>> getHistory(@RequestParam Long userId) {
        return ResponseEntity.ok(service.getActivityHistory(userId));
    }

    // ----------------------------
    // 4) Actividades por rango
    // ----------------------------
    @GetMapping("/range")
    public ResponseEntity<List<ActivityHistoryDTO>> getRange(
            @RequestParam Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        return ResponseEntity.ok(service.getActivitiesByRange(userId, startDate, endDate));
    }

    // ----------------------------
    // 5) Totales acumulados
    // ----------------------------
    @GetMapping("/totals")
    public ResponseEntity<TotalsReportDTO> getTotals(@RequestParam Long userId) {
        return ResponseEntity.ok(service.getTotals(userId));
    }
}