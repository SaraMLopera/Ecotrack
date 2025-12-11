package ecotrack.backend.services;

import ecotrack.backend.dto.ActivityHistoryDTO;
import ecotrack.backend.dto.StatsByTypeDTO;
import ecotrack.backend.dto.TotalsReportDTO;
import ecotrack.backend.models.entitys.CarbonRecord;
import ecotrack.backend.models.entitys.CarbonTotal;
import ecotrack.backend.models.entitys.DailyActivity;
import ecotrack.backend.models.entitys.User;
import ecotrack.backend.repositories.CarbonRecordRepository;
import ecotrack.backend.repositories.CarbonTotalRepository;
import ecotrack.backend.repositories.DailyActivityRepository;
import ecotrack.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final UserRepository userRepository;
    private final DailyActivityRepository activityRepository;
    private final CarbonRecordRepository recordRepository;
    private final CarbonTotalRepository totalRepository;

    public ReportService(UserRepository userRepository,
                        DailyActivityRepository activityRepository,
                        CarbonRecordRepository recordRepository,
                        CarbonTotalRepository totalRepository) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.recordRepository = recordRepository;
        this.totalRepository = totalRepository;
    }

    /**
     * Obtener todas las actividades de un usuario
     */
    public List<ActivityHistoryDTO> getUserActivities(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        List<DailyActivity> activities = activityRepository.findByUserIdOrderByFechaDesc(userId);
        
        return activities.stream().map(activity -> {
            // Buscar emisión correspondiente
            Double emisiones = recordRepository.findByActivityUserId(userId).stream()
                    .filter(record -> record.getActivity().getId().equals(activity.getId()))
                    .findFirst()
                    .map(CarbonRecord::getEmisiones_calculadas)
                    .orElse(0.0);

            return ActivityHistoryDTO.builder()
                    .id(activity.getId())
                    .fecha(activity.getFecha())
                    .tipoActividad(activity.getTipo_actividad())
                    .descripcion(activity.getDescripcion())
                    .emisiones(emisiones)
                    .activityId(activity.getActivityId())
                    .region(activity.getRegion())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Obtener actividades por rango de fechas
     */
    public List<ActivityHistoryDTO> getUserActivitiesByDateRange(Long userId, String startDate, String endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        List<DailyActivity> activities = activityRepository
                .findByUserIdAndFechaBetween(userId, startDate, endDate);
        
        return activities.stream().map(activity -> {
            Double emisiones = recordRepository.findByActivityUserId(userId).stream()
                    .filter(record -> record.getActivity().getId().equals(activity.getId()))
                    .findFirst()
                    .map(CarbonRecord::getEmisiones_calculadas)
                    .orElse(0.0);

            return ActivityHistoryDTO.builder()
                    .id(activity.getId())
                    .fecha(activity.getFecha())
                    .tipoActividad(activity.getTipo_actividad())
                    .descripcion(activity.getDescripcion())
                    .emisiones(emisiones)
                    .activityId(activity.getActivityId())
                    .region(activity.getRegion())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Obtener totales de carbono del usuario
     */
    public TotalsReportDTO getUserTotals(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        CarbonTotal total = totalRepository.findByUserId(userId)
                .orElseGet(() -> {
                    CarbonTotal newTotal = new CarbonTotal();
                    newTotal.setUser(user);
                    newTotal.setTotal_historico(0.0);
                    newTotal.setTotal_mensual(0.0);
                    newTotal.setTotal_semanal(0.0);
                    return newTotal;
                });

        Long actividadesCount = activityRepository.countByUserId(userId);

        return TotalsReportDTO.builder()
                .userId(userId)
                .userName(user.getNombre())
                .totalHistorico(total.getTotal_historico())
                .totalMensual(total.getTotal_mensual())
                .totalSemanal(total.getTotal_semanal())
                .actividadesRegistradas(actividadesCount.intValue())
                .build();
    }

    /**
     * Obtener estadísticas por tipo de actividad
     */
    public StatsByTypeDTO getUserStatsByType(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        List<DailyActivity> activities = activityRepository.findByUserIdOrderByFechaDesc(userId);
        List<CarbonRecord> records = recordRepository.findByActivityUserId(userId);

        // Agrupar emisiones por tipo
        Map<String, Double> emisionesPorTipo = new HashMap<>();
        Map<String, Integer> actividadesPorTipo = new HashMap<>();

        for (DailyActivity activity : activities) {
            String tipo = activity.getTipo_actividad();
            
            // Sumar emisiones
            Double emision = records.stream()
                    .filter(record -> record.getActivity().getId().equals(activity.getId()))
                    .findFirst()
                    .map(CarbonRecord::getEmisiones_calculadas)
                    .orElse(0.0);
            
            emisionesPorTipo.merge(tipo, emision, Double::sum);
            actividadesPorTipo.merge(tipo, 1, Integer::sum);
        }

        // Encontrar el tipo más contaminante
        String tipoMasContaminante = emisionesPorTipo.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        // Calcular total de emisiones
        Double totalEmisiones = emisionesPorTipo.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        return StatsByTypeDTO.builder()
                .userId(userId)
                .emisionesPorTipo(emisionesPorTipo)
                .actividadesPorTipo(actividadesPorTipo)
                .tipoMasContaminante(tipoMasContaminante)
                .totalEmisiones(totalEmisiones)
                .build();
    }
}