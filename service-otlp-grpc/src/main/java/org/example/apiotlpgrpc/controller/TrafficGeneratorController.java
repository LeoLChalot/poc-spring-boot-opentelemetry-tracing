package org.example.apiotlpgrpc.controller;

import io.opentelemetry.api.trace.Span;
import org.example.apiotlpgrpc.service.TraceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class TrafficGeneratorController {

    private final TraceService traceService;
    private final RestTemplate restTemplate;

    public TrafficGeneratorController(TraceService traceService, RestTemplate restTemplate) {
        this.traceService = traceService;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/api/orchestrate")
    public String scenarioComplexe() {
        // Span Parent "RACINE"
        return traceService.wrapWithSpan("orchestration_flux_complet", (Span span) -> {

            span.setAttribute("scenario", "complex_chain");

            // Appel 1 : Récupération info utilisateur (Simulé)
            span.addEvent("Début de l'appel vers API Process");
            String response1 = restTemplate.getForObject("http://localhost:8080/api/process?user=Manager", String.class);

            // Appel 2 : Notification (Void/Async simulé)
            span.addEvent("Début de l'appel vers API Notify");
            String response2 = restTemplate.getForObject("http://localhost:8080/api/notify", String.class);

            // Appel 3 : Tentative risquée
            span.addEvent("Début de l'appel vers API Risky");
            try {
                restTemplate.getForObject("http://localhost:8080/api/risky", String.class);
            } catch (Exception e) {
                span.addEvent("L'API Risky a échoué (comme prévu parfois), mais on continue le flux.");
            }

            return "Orchestration terminée. Réponses : " + response1 + " / " + response2;
        });
    }
}