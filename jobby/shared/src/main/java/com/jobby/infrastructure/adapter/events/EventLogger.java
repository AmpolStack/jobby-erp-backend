package com.jobby.infrastructure.adapter.events;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public class EventLogger {

    private final ObservationRegistry observationRegistry;

    public void logReceived(Object event, Map<String, Object> headers) {
        MDC.put("eventType", event.getClass().getSimpleName());
        var observation = Observation.createNotStarted("event.receive", observationRegistry)
                .lowCardinalityKeyValue("eventType", event.getClass().getSimpleName())
                .start();
        try {
            var eventId = extractHeader(headers, "eventId");
            var aggregateType = extractHeader(headers, "aggregateType");
            var operationType = extractHeader(headers, "operationType");

            log.info("Event received: type={}, eventId={}, aggregateType={}, operationType={}",
                    event.getClass().getSimpleName(), eventId, aggregateType, operationType);

            observation.stop();
        } catch (Exception e) {
            observation.error(e);
            log.warn("[ITS_EXTERNAL_SERVICE_FAILURE] Event logging failed: type={}", event.getClass().getSimpleName(), e);
        } finally {
            MDC.remove("eventType");
        }
    }

    public void logProcessed(Object event, Map<String, Object> headers) {
        MDC.put("eventType", event.getClass().getSimpleName());
        var observation = Observation.createNotStarted("event.process", observationRegistry)
                .lowCardinalityKeyValue("eventType", event.getClass().getSimpleName())
                .start();
        try {
            log.info("Event processed successfully: type={}", event.getClass().getSimpleName());
            observation.stop();
        } catch (Exception e) {
            observation.error(e);
        } finally {
            MDC.remove("eventType");
        }
    }

    public void logError(Object event, Map<String, Object> headers, Throwable error) {
        MDC.put("eventType", event.getClass().getSimpleName());
        var observation = Observation.createNotStarted("event.error", observationRegistry)
                .lowCardinalityKeyValue("eventType", event.getClass().getSimpleName())
                .start();
        try {
            var eventId = extractHeader(headers, "eventId");
            var aggregateType = extractHeader(headers, "aggregateType");
            var operationType = extractHeader(headers, "operationType");

            log.error("[ITS_EXTERNAL_SERVICE_FAILURE] Event processing failed: type={}, eventId={}, aggregateType={}, operationType={}",
                    event.getClass().getSimpleName(), eventId, aggregateType, operationType, error);

            observation.error(error);
        } finally {
            MDC.remove("eventType");
        }
    }

    public void logResult(Object event, Map<String, Object> headers, Result<?, Error> result) {
        if (result.isSuccess()) {
            logProcessed(event, headers);
        } else {
            var error = result.error();
            logError(event, headers, new RuntimeException(error.getCode().name() + ": " + error.getFields()));
        }
    }

    private static String extractHeader(Map<String, Object> headers, String key) {
        var value = headers.get(key);
        if (value == null) return "N/A";
        if (value instanceof byte[] bytes) return new String(bytes, StandardCharsets.UTF_8);
        return value.toString();
    }
}
