package ecotrack.backend.carbonApi.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class ClimatiqService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DATA_VERSION = "^28";
    
    @Value("${climatiq.api.key}")
    private String apiKey;

    public ClimatiqService(
            @Value("${climatiq.api.url}") String apiUrl,
            @Value("${climatiq.api.key}") String apiKey) {

        System.out.println("🔧 ClimatiqService inicializado:");
        System.out.println("   API URL: '" + apiUrl + "'");
        System.out.println("   API Key presente: " + (apiKey != null && !apiKey.isEmpty()));
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("❌ ERROR CRÍTICO: API Key está VACÍA o NULA!");
            System.err.println("   Verifica application.properties");
        } else {
            System.out.println("   API Key length: " + apiKey.length());
            System.out.println("   API Key primeros 10 chars: " + apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
        }

        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
                
        System.out.println("✅ WebClient configurado correctamente");
    }

    /**
     * Calcula emisiones usando Climatiq API
     */
    public Double calculateEmission(String activityId, String region, Map<String, Object> parameters) {
        System.out.println("\n🌍 ============ INICIO CÁLCULO CLIMATIQ ============");
        System.out.println("📋 Datos recibidos:");
        System.out.println("   Activity ID: " + activityId);
        System.out.println("   Region: " + region);
        System.out.println("   Parameters: " + parameters);
        System.out.println("   Data Version: " + DATA_VERSION);

        // Validaciones
        if (activityId == null || activityId.isEmpty()) {
            System.err.println("❌ ERROR: activityId es nulo o vacío");
            return calculateFallback(parameters);
        }

        if (parameters == null || parameters.isEmpty()) {
            System.err.println("❌ ERROR: parameters es nulo o vacío");
            return 0.0;
        }

        try {
            // Construir emission_factor
            Map<String, Object> emissionFactor = new HashMap<>();
            emissionFactor.put("activity_id", activityId);
            emissionFactor.put("data_version", DATA_VERSION);
            
            if (region != null && !region.isEmpty() && !region.equalsIgnoreCase("GLOBAL")) {
                emissionFactor.put("region", region);
            }

            Map<String, Object> body = new HashMap<>();
            body.put("emission_factor", emissionFactor);
            body.put("parameters", parameters);

            System.out.println("📤 Request Body enviado a Climatiq:");
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));

            // Llamar a la API con timeout
            Map response = webClient.post()
                    .uri("/estimate")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(java.time.Duration.ofSeconds(10))
                    .doOnError(WebClientResponseException.class, error -> {
                        System.err.println("❌ Error HTTP de Climatiq:");
                        System.err.println("   Status Code: " + error.getStatusCode());
                        System.err.println("   Response Body: " + error.getResponseBodyAsString());
                    })
                    .block();

            System.out.println("📥 Respuesta de Climatiq:");
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));

            // Extraer co2e
            if (response != null && response.containsKey("co2e")) {
                Double co2e = ((Number) response.get("co2e")).doubleValue();
                System.out.println("✅ Emisión calculada por Climatiq: " + co2e + " kg CO2e");
                System.out.println("============ FIN CÁLCULO CLIMATIQ ============\n");
                return co2e;
            } else if (response != null && response.containsKey("co2e_total")) {
                Double co2e = ((Number) response.get("co2e_total")).doubleValue();
                System.out.println("✅ Emisión calculada por Climatiq (total): " + co2e + " kg CO2e");
                System.out.println("============ FIN CÁLCULO CLIMATIQ ============\n");
                return co2e;
            }

            System.err.println("⚠️ Respuesta de Climatiq sin campo 'co2e'");
            System.err.println("   Campos disponibles: " + (response != null ? response.keySet() : "null"));
            
            // Usar fallback
            Double fallback = calculateFallback(parameters);
            System.out.println("🔄 Usando cálculo local: " + fallback + " kg CO2e");
            System.out.println("============ FIN CÁLCULO CLIMATIQ ============\n");
            return fallback;

        } catch (WebClientResponseException e) {
            System.err.println("❌ ERROR HTTP DE CLIMATIQ:");
            System.err.println("   Status: " + e.getStatusCode());
            System.err.println("   Response: " + e.getResponseBodyAsString());
            System.err.println("   Message: " + e.getMessage());
            
            // Usar fallback
            Double fallback = calculateFallback(parameters);
            System.out.println("🔄 Usando cálculo local por error HTTP: " + fallback + " kg CO2e");
            System.out.println("============ FIN CÁLCULO CLIMATIQ ============\n");
            return fallback;
            
        } catch (Exception e) {
            System.err.println("❌ ERROR INESPERADO:");
            System.err.println("   Tipo: " + e.getClass().getName());
            System.err.println("   Mensaje: " + e.getMessage());
            e.printStackTrace();
            
            // Usar fallback
            Double fallback = calculateFallback(parameters);
            System.out.println("🔄 Usando cálculo local por error: " + fallback + " kg CO2e");
            System.out.println("============ FIN CÁLCULO CLIMATIQ ============\n");
            return fallback;
        }
    }

    /**
     * Cálculo local de emergencia si Climatiq falla
     */
    private Double calculateFallback(Map<String, Object> parameters) {
        System.out.println("🧮 Iniciando cálculo local de emergencia...");
        
        // Factores de emisión aproximados (kg CO2 por unidad)
        double value = 0.0;
        double factor = 0.5; // default
        
        if (parameters.containsKey("energy")) {
            value = ((Number) parameters.get("energy")).doubleValue();
            factor = 0.5; // kg CO2 por kWh (electricidad)
            System.out.println("   Detectado: ELECTRICIDAD");
        } else if (parameters.containsKey("distance")) {
            value = ((Number) parameters.get("distance")).doubleValue();
            factor = 0.2; // kg CO2 por km (transporte promedio)
            System.out.println("   Detectado: TRANSPORTE");
        } else if (parameters.containsKey("volume")) {
            value = ((Number) parameters.get("volume")).doubleValue();
            factor = 2.3; // kg CO2 por litro (combustible)
            System.out.println("   Detectado: COMBUSTIBLE");
        } else if (parameters.containsKey("weight")) {
            value = ((Number) parameters.get("weight")).doubleValue();
            factor = 0.3; // kg CO2 por kg (residuos)
            System.out.println("   Detectado: RESIDUOS");
        }
        
        Double result = value * factor;
        System.out.println("   Valor: " + value);
        System.out.println("   Factor: " + factor);
        System.out.println("   Resultado: " + result + " kg CO2e");
        
        return result;
    }

    /**
     * Buscar activity IDs disponibles en Climatiq
     */
    public Mono<String> searchActivityIds(String query, String region) {
        System.out.println("🔍 Buscando activity IDs:");
        System.out.println("   Query: " + query);
        System.out.println("   Region: " + region);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/search")
                    .queryParam("query", query)
                    .queryParam("region", region)
                    .queryParam("data_version", DATA_VERSION)
                    .build())
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> System.out.println("✅ Búsqueda exitosa"))
                .doOnError(WebClientResponseException.class, error -> {
                    System.err.println("❌ Error en búsqueda:");
                    System.err.println("   Status: " + error.getStatusCode());
                    System.err.println("   Response: " + error.getResponseBodyAsString());
                })
                .doOnError(e -> System.err.println("❌ Error inesperado: " + e.getMessage()));
    }
}
