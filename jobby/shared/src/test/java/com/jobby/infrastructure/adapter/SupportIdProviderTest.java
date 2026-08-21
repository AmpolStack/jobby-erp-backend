package com.jobby.infrastructure.adapter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("SupportIdProvider - Unit Tests")
@ExtendWith(MockitoExtension.class)
class SupportIdProviderTest {

    @Mock
    private Tracer tracer;

    @Mock
    private Span span;

    @Mock
    private TraceContext traceContext;

    private SupportIdProvider provider;

    @BeforeEach
    void setUp() {
        provider = new SupportIdProvider(tracer);
    }

    @Test
    @DisplayName("current: active span with trace ID returns Optional with trace ID")
    void givenActiveSpanWithTraceId_whenCurrent_returnsOptionalWithTraceId() {
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn("abc123def456");

        var result = provider.current();

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("abc123def456");
    }

    @Test
    @DisplayName("current: no active span returns Optional.empty")
    void givenNoActiveSpan_whenCurrent_returnsEmptyOptional() {
        when(tracer.currentSpan()).thenReturn(null);

        var result = provider.current();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("current: blank trace ID returns Optional.empty")
    void givenBlankTraceId_whenCurrent_returnsEmptyOptional() {
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn("   ");

        var result = provider.current();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("current: empty trace ID returns Optional.empty")
    void givenEmptyTraceId_whenCurrent_returnsEmptyOptional() {
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn("");

        var result = provider.current();

        assertThat(result).isEmpty();
    }
}
