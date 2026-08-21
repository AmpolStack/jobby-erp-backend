package com.jobby.infrastructure.bus;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.in.Event;
import com.jobby.domain.ports.in.EventBus;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MetricsEventBusDecorator implements EventBus {

    private final EventBus delegate;
    private final MeterRegistry meterRegistry;

    @Override
    @SuppressWarnings("unchecked")
    public <TResponse> Result<TResponse, Error> dispatch(Event event) {
        var sample = Timer.start();
        var result = (Result<TResponse, Error>) delegate.dispatch(event);
        sample.stop(Timer.builder("event.bus.duration")
                .tag("event", event.getClass().getSimpleName())
                .tag("success", String.valueOf(result.isSuccess()))
                .register(meterRegistry));
        return result;
    }
}
