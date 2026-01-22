package org.example.apiotlpgrpc.controller;

import io.opentelemetry.api.trace.Span;
import org.example.tracing.annotation.Monitored;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;


@RestController
public class OtlpRestController {

    private final Random random = new Random();
    private static final Logger logger = LoggerFactory.getLogger(OtlpRestController.class);

    /**
     * Cas 1 : Succès Standard
     */
    @GetMapping("/api/process")
    @Monitored("traitement_standard")
    public String lancerTraitement(@RequestParam(defaultValue = "anonyme") String user) throws InterruptedException {
        logger.info("Début du traitement métier pour {}", user);
        Thread.sleep(200);
        return "Traitement réussi pour " + user;
    }

    /**
     * Cas 2 : Erreur Standard
     */
    @GetMapping("/api/risky")
    @Monitored("traitement_risque")
    public String lancerTraitementRisque() {
        simulerLatence(50, 150);
        if (random.nextBoolean()) {
            throw new RuntimeException("Critical issues");
        }
        return "Succès\n";
    }

    /**
     * Cas 3 : Void
     * Tâches qui ne envoient pas de données métier.
     */
    @GetMapping("/api/notify")
    @Monitored("envoi_notification")
    public String envoyerNotification() {
        Span currentSpan = Span.current();
        currentSpan.setAttribute("notification.type", "EMAIL");
        currentSpan.setAttribute("notification.recipient", "admin@example.com");
        System.out.println("Envoi de l'email en cours...");
        simulerLatence(200, 500);
        currentSpan.addEvent("Email envoyé au serveur SMTP");
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