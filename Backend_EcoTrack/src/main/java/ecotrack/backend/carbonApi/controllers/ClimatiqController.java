package ecotrack.backend.carbonApi.controllers;

import ecotrack.backend.carbonApi.services.ClimatiqService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/climatiq")
public class ClimatiqController {

    private final ClimatiqService climatiqService;

    public ClimatiqController(ClimatiqService climatiqService) {
        this.climatiqService = climatiqService;
    }

    @GetMapping("/search")
    public Mono<String> searchActivities(
            @RequestParam String query,
            @RequestParam(defaultValue = "CO") String region) {
        return climatiqService.searchActivityIds(query, region);
    }
}