package org.example.tracing.config;

import io.opentelemetry.api.OpenTelemetry; // Import de l'objet racine
import io.opentelemetry.api.trace.Tracer;
import org.example.tracing.aspect.TracingAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingAutoConfiguration {

    @Bean
    public TracingAspect tracingAspect(OpenTelemetry openTelemetry) {
        Tracer tracer = openTelemetry.getTracer("org.example.app-tracing", "1.0.0");

        return new TracingAspect(tracer);
    }
}