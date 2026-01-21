package org.example.apiotlpgrpc.controller;

import io.opentelemetry.api.trace.Span;
import org.example.apiotlpgrpc.service.TraceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;


@RestController
public class OtlpRestController {

    private final TraceService traceService;
    private final Random random = new Random();

    public OtlpRestController(TraceService traceService) {
        this.traceService = traceService;
    }

    /**
     * Cas 1 : Succès Standard
     */
    @GetMapping("/api/process")
    public String lancerTraitement(@RequestParam(defaultValue = "anonyme") String user) {
        return traceService.wrapWithSpan("traitement_standard", (Span span) -> {
            span.setAttribute("app.user", user);
            simulerLatence(100, 300); // Latence variable
            span.addEvent("Étape 1 validée");
            simulerLatence(50, 100);
            span.addEvent("Étape 2 validée");
            return "Traitement réussi pour " + user;
        });
    }

    /**
     * Cas 2 : Erreur Standard
     */
    @GetMapping("/api/risky")
    public String lancerTraitementRisque() {
        return traceService.wrapWithSpan("traitement_risque", (Span span) -> {
            span.setAttribute("risk.level", "high");
            simulerLatence(50, 150);

            // Probabilité d'erreur aléatoire
            if (random.nextBoolean()) {
                throw new RuntimeException("Critical issues");
            }

            span.addEvent("Success");
            return "Succès\n";
        });
    }

    /**
     * Cas 3 : Void
     * Tâches qui ne envoient pas de données métier.
     */
    @GetMapping("/api/notify")
    public String envoyerNotification() {
        traceService.wrapWithSpanVoid("envoi_notification", (Span span) -> {
            span.setAttribute("notification.type", "EMAIL");
            span.setAttribute("notification.recipient", "admin@example.com");

            System.out.println("Envoi de l'email en cours...");
            simulerLatence(200, 500);

            span.addEvent("Email envoyé au serveur SMTP");
        });
        return "Notification envoyée.";
    }

    /**
     * Simulation du trafic
     */
    private void simulerLatence(int min, int max) {
        try {
            int duree = random.nextInt(max - min) + min;
            Thread.sleep(duree);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}