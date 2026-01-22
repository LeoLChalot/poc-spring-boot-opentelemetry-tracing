package org.example.tracing.aspect;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.tracing.annotation.Monitored;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TracingAspect {

    private final Tracer tracer;

    public TracingAspect(Tracer tracer) {
        this.tracer = tracer;
    }

    // Intercepte toutes les méthodes annotées avec @Monitored
    @Around("@annotation(monitored)")
    public Object traceMethod(ProceedingJoinPoint joinPoint, Monitored monitored) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();

        // Nom du Span
        String spanName = monitored.value().isEmpty() ? methodName : monitored.value();

        // Démarrer le Span
        Span span = tracer.spanBuilder(spanName).startSpan();

        // Ajout des arguments
        Object[] args = joinPoint.getArgs();
        String[] paramNames = signature.getParameterNames();

        if (paramNames != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    span.setAttribute("app.arg." + paramNames[i], args[i].toString());
                }
            }
        }

        // Exécution dans le scope du Span
        try (Scope scope = span.makeCurrent()) {
            Object result = joinPoint.proceed(); // Appel de la vraie méthode
            return result;
        } catch (Throwable t) {
            span.recordException(t);
            span.setStatus(StatusCode.ERROR, t.getMessage());
            throw t;
        } finally {
            // Fermeture du Span
            span.end();
        }
    }
}