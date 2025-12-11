package ecotrack.backend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecotrack.backend.carbonApi.services.ClimatiqService;
import ecotrack.backend.dto.*;
import ecotrack.backend.models.entitys.*;
import ecotrack.backend.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private final UserRepository userRepo;
    private final DailyActivityRepository dailyRepo;
    private final CarbonRecordRepository recordRepo;
    private final CarbonTotalRepository totalRepo;
    private final ClimatiqService climatiqService;

    private final ObjectMapper mapper = new ObjectMapper();

    public ActivityService(
            UserRepository userRepo,
            DailyActivityRepository dailyRepo,
            CarbonRecordRepository recordRepo,
            CarbonTotalRepository totalRepo,
            ClimatiqService climatiqService
    ) {
        this.userRepo = userRepo;
        this.dailyRepo = dailyRepo;
        this.recordRepo = recordRepo;
        this.totalRepo = totalRepo;
        this.climatiqService = climatiqService;
    }

    // ------------------------------------------------------
    // 1) Registrar una actividad
    // ------------------------------------------------------
    @Transactional
    public ActivityResponse registerActivity(RegisterActivityRequest req) {
        
        System.out.println("📥 Recibido en Service:");
        System.out.println("   UserId: " + req.getUserId());
        System.out.println("   Fecha: " + req.getFecha());
        System.out.println("   Tipo Actividad: " + req.getTipoActividad());
        System.out.println("   Descripción: " + req.getDescripcion());
        System.out.println("   ActivityId: " + req.getActivityId());
        System.out.println("   Categoría: " + req.getPoactividad());
        
        User user = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Región por defecto
        String region = req.getRegion() != null ? req.getRegion() : "CO";

        // 🔧 CONSTRUCCIÓN DEL MAP DE PARÁMETROS SEGÚN TIPO DE ACTIVIDAD
        Map<String, Object> climatiqParameters = buildClimatiqParameters(req);
        
        System.out.println("🔧 Parámetros construidos para Climatiq:");
        System.out.println("   " + climatiqParameters);

        // Cálculo con Climatiq
        Double emisiones = climatiqService.calculateEmission(
                req.getActivityId(),
                region,
                climatiqParameters  // ✅ Ahora sí tiene datos
        );
        
        System.out.println("✅ Emisiones calculadas: " + emisiones + " kg CO2");

        // Convertir parámetros a JSON para guardar en BD
        String parametersJson = "";
        try {
            parametersJson = mapper.writeValueAsString(climatiqParameters);
            System.out.println("💾 Parameters JSON: " + parametersJson);
        } catch (Exception e) {
            System.err.println("❌ Error al serializar parámetros: " + e.getMessage());
            throw new RuntimeException("Error al serializar parámetros", e);
        }

        // Guardar actividad
        DailyActivity activity = DailyActivity.builder()
                .fecha(req.getFecha())
                .tipo_actividad(req.getTipoActividad())
                .descripcion(req.getDescripcion())
                .activityId(req.getActivityId())
                .parameters(parametersJson)
                .region(region)
                .user(user)
                .build();

        activity = dailyRepo.save(activity);
        System.out.println("💾 Actividad guardada con ID: " + activity.getId());

        // Guardar el registro histórico
        CarbonRecord record = CarbonRecord.builder()
                .activity(activity)
                .emisiones_calculadas(emisiones)
                .fuente("climatiq")
                .build();
        recordRepo.save(record);
        System.out.println("💾 Registro de carbono guardado");

        // Actualizar totales
        CarbonTotal total = totalRepo.findByUserId(user.getId())
                .orElseGet(() -> {
                    System.out.println("🆕 Creando nuevo registro de totales para usuario " + user.getId());
                    CarbonTotal t = new CarbonTotal();
                    t.setUser(user);
                    t.setTotal_historico(0.0);
                    t.setTotal_mensual(0.0);
                    t.setTotal_semanal(0.0);
                    return t;
                });

        Double nuevoHistorico = total.getTotal_historico() + emisiones;
        Double nuevoMensual = total.getTotal_mensual() + emisiones;
        Double nuevoSemanal = total.getTotal_semanal() + emisiones;
        
        total.setTotal_historico(nuevoHistorico);
        total.setTotal_mensual(nuevoMensual);
        total.setTotal_semanal(nuevoSemanal);
        totalRepo.save(total);
        
        System.out.println("📊 Totales actualizados:");
        System.out.println("   Histórico: " + nuevoHistorico);
        System.out.println("   Mensual: " + nuevoMensual);
        System.out.println("   Semanal: " + nuevoSemanal);

        return ActivityResponse.builder()
        .activityId(activity.getId())
        .fecha(activity.getFecha())
        .tipoActividad(activity.getTipo_actividad())
        .descripcion(activity.getDescripcion())
        .poactividad(req.getPoactividad())  // ✅ NUEVO
        .climatiqActivityId(req.getActivityId())  // ✅ NUEVO
        .region(region)  // ✅ NUEVO
        .co2e(emisiones)
        .fuente("climatiq")
        .parametersUsed(parametersJson)  // ✅ NUEVO (opcional, para debug)
        .build();
    }

    // 🔧 MÉTODO NUEVO: Construir parámetros según tipo de actividad
    // 🔧 NUEVO MÉTODO DEFINITIVO: Construir parámetros según activityId (NO por texto de categoría)
private Map<String, Object> buildClimatiqParameters(RegisterActivityRequest req) {
    Map<String, Object> params = new HashMap<>();

    String id = req.getActivityId();
    if (id == null) {
        throw new RuntimeException("activityId no puede ser null");
    }

    id = id.toLowerCase();

    System.out.println("🔍 Construyendo parámetros usando activityId: " + id);

    // ---------------------------------------
    // 🚗 1. TRANSPORTE
    // ---------------------------------------
    if (id.contains("transport") || id.contains("vehicle") || id.contains("passenger") || id.contains("car")) {

        if (req.getTransportation_distance() == null || req.getTransportation_distance().isEmpty()) {
            throw new RuntimeException("Falta distance para transporte");
        }

        params.put("distance", Double.parseDouble(req.getTransportation_distance()));
        params.put("distance_unit", req.getTransportation_unit() != null ? req.getTransportation_unit() : "km");

        System.out.println("   ✅ Tipo detectado: TRANSPORTE");
        System.out.println("   distance=" + params.get("distance") + " " + params.get("distance_unit"));
        return params;
    }

    // ---------------------------------------
    // ⛽ 2. COMBUSTIBLE
    // ---------------------------------------
    if (id.contains("fuel") || id.contains("diesel") || id.contains("petrol") || id.contains("gasoline")) {

        if (req.getFuel_source_volume() == null || req.getFuel_source_volume().isEmpty()) {
            throw new RuntimeException("Falta volumen para combustible");
        }

        params.put("volume", Double.parseDouble(req.getFuel_source_volume()));
        params.put("volume_unit", req.getFuel_source_unit() != null ? req.getFuel_source_unit() : "L");

        System.out.println("   ✅ Tipo detectado: COMBUSTIBLE");
        System.out.println("   volume=" + params.get("volume") + " " + params.get("volume_unit"));
        return params;
    }

    // ---------------------------------------
    // ⚡ 3. ELECTRICIDAD
    // ---------------------------------------
    if (id.contains("electricity") || id.contains("energy") || id.contains("power")) {

        if (req.getElectricity_value() == null || req.getElectricity_value().isEmpty()) {
            throw new RuntimeException("Falta valor de electricidad");
        }

        params.put("energy", Double.parseDouble(req.getElectricity_value()));
        params.put("energy_unit", req.getElectricity_unit() != null ? req.getElectricity_unit() : "kWh");

        System.out.println("   ✅ Tipo detectado: ELECTRICIDAD");
        System.out.println("   energy=" + params.get("energy") + " " + params.get("energy_unit"));
        return params;
    }

    // ❌ Si no coincide con nada → error
    System.err.println("❌ No se pudieron construir parámetros. ActivityId no coincide con ningún tipo conocido.");
    throw new RuntimeException("No se pudieron extraer parámetros de la actividad. ActivityId=" + id);
}


    // ------------------------------------------------------
    // 2) Actividades del día
    // ------------------------------------------------------
    public List<ActivityHistoryDTO> getDailyActivities(Long userId, String fecha) {
        System.out.println("🔍 Buscando actividades del día para userId=" + userId + ", fecha=" + fecha);
        
        List<DailyActivity> activities = dailyRepo.findByUserIdAndFecha(userId, fecha);
        System.out.println("   Encontradas: " + activities.size() + " actividades");
        
        return activities.stream()
                .map(a -> {
                    Double emisiones = recordRepo.findByActivityUserId(userId).stream()
                            .filter(r -> r.getActivity().getId().equals(a.getId()))
                            .findFirst()
                            .map(CarbonRecord::getEmisiones_calculadas)
                            .orElse(0.0);
                    
                    return ActivityHistoryDTO.builder()
                            .id(a.getId())
                            .fecha(a.getFecha())
                            .tipoActividad(a.getTipo_actividad())
                            .descripcion(a.getDescripcion())
                            .activityId(a.getActivityId())
                            .region(a.getRegion())
                            .emisiones(emisiones)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------
    // 3) Historial completo
    // ------------------------------------------------------
    public List<ActivityHistoryDTO> getActivityHistory(Long userId) {
        System.out.println("📜 Obteniendo historial completo para userId=" + userId);
        
        List<DailyActivity> activities = dailyRepo.findByUserIdOrderByFechaDesc(userId);
        System.out.println("   Total de actividades: " + activities.size());
        
        return activities.stream()
                .map(a -> {
                    Double emisiones = recordRepo.findByActivityUserId(userId).stream()
                            .filter(r -> r.getActivity().getId().equals(a.getId()))
                            .findFirst()
                            .map(CarbonRecord::getEmisiones_calculadas)
                            .orElse(0.0);
                    
                    return ActivityHistoryDTO.builder()
                            .id(a.getId())
                            .fecha(a.getFecha())
                            .tipoActividad(a.getTipo_actividad())
                            .descripcion(a.getDescripcion())
                            .activityId(a.getActivityId())
                            .region(a.getRegion())
                            .emisiones(emisiones)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------
    // 4) Actividades por rango
    // ------------------------------------------------------
    public List<ActivityHistoryDTO> getActivitiesByRange(Long userId, String start, String end) {
        System.out.println("📅 Buscando actividades por rango:");
        System.out.println("   userId=" + userId + ", desde=" + start + ", hasta=" + end);
        
        List<DailyActivity> activities = dailyRepo.findByUserIdAndFechaBetween(userId, start, end);
        System.out.println("   Encontradas: " + activities.size() + " actividades");
        
        return activities.stream()
                .map(a -> {
                    Double emisiones = recordRepo.findByActivityUserId(userId).stream()
                            .filter(r -> r.getActivity().getId().equals(a.getId()))
                            .findFirst()
                            .map(CarbonRecord::getEmisiones_calculadas)
                            .orElse(0.0);
                    
                    return ActivityHistoryDTO.builder()
                            .id(a.getId())
                            .fecha(a.getFecha())
                            .tipoActividad(a.getTipo_actividad())
                            .descripcion(a.getDescripcion())
                            .activityId(a.getActivityId())
                            .region(a.getRegion())
                            .emisiones(emisiones)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------
    // 5) Totales acumulados
    // ------------------------------------------------------
    public TotalsReportDTO getTotals(Long userId) {
        System.out.println("📊 Obteniendo totales para userId=" + userId);
        
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        CarbonTotal total = totalRepo.findByUserId(userId)
                .orElseGet(() -> {
                    System.out.println("⚠️  No hay totales registrados, retornando valores en 0");
                    CarbonTotal t = new CarbonTotal();
                    t.setTotal_historico(0.0);
                    t.setTotal_mensual(0.0);
                    t.setTotal_semanal(0.0);
                    return t;
                });

        Integer registros = dailyRepo.countByUserId(userId).intValue();
        
        System.out.println("   Histórico: " + total.getTotal_historico());
        System.out.println("   Mensual: " + total.getTotal_mensual());
        System.out.println("   Semanal: " + total.getTotal_semanal());
        System.out.println("   Registros: " + registros);

        return TotalsReportDTO.builder()
                .userId(userId)
                .userName(user.getNombre())
                .totalHistorico(total.getTotal_historico())
                .totalMensual(total.getTotal_mensual())
                .totalSemanal(total.getTotal_semanal())
                .actividadesRegistradas(registros)
                .build();
    }
}