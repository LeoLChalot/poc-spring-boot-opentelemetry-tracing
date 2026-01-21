package org.example.apiotlpgrpc.service;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class TraceService {

    private final Tracer tracer;

    // Injection du Tracer configuré dans OtelConfiguration
    public TraceService(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Exécute une logique métier qui retourne un résultat (T) à l'intérieur d'un Span.
     * @param spanName Le nom de l'opération
     * @param businessLogic La fonction à exécuter (accepte le Span en paramètre pour ajouter des events/attributs)
     * @return Le résultat de la logique métier
     */
    public <T> T wrapWithSpan(String spanName, Function<Span, T> businessLogic) {
        Span span = tracer.spanBuilder(spanName).startSpan();
        try (Scope scope = span.makeCurrent()) {
            return businessLogic.apply(span);
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Surcharge pour les méthodes void
     */
    public void wrapWithSpanVoid(String spanName, Consumer<Span> businessLogic) {
        wrapWithSpan(spanName, span -> {
            businessLogic.accept(span);
            return null;
        });
    }
}