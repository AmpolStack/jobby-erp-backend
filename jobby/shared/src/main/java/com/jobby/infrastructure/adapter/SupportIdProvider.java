package com.jobby.infrastructure.adapter;

import io.micrometer.tracing.Tracer;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class SupportIdProvider {

    private final Tracer tracer;

    public Optional<String> current() {
        var span = tracer.currentSpan();
        if (span == null) return Optional.empty();

        var traceId = span.context().traceId();
        return Optional.of(traceId)
                .filter(id -> !id.isBlank());
    }
}