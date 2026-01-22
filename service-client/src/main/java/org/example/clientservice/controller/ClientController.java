package org.example.clientservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.tracing.annotation.Monitored;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);
    private final RestTemplate restTemplate;


    @Value("${service.a.url:http://service-otlp-grpc:8080}")
    private String serviceAUrl;

    public ClientController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/api/chain")
    @Monitored("orchestration_client")
    public String callServiceA() {
        logger.info("Service Client : Appel vers {}", serviceAUrl);

        String url = serviceAUrl + "/api/process?user=FromDocker";
        String response = restTemplate.getForObject(url, String.class);

        return "Chaîne terminée ! Réponse : " + response;
    }
}