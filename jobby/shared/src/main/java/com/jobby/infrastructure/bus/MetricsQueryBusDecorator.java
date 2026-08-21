package com.jobby.infrastructure.bus;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.in.Query;
import com.jobby.domain.ports.in.QueryBus;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MetricsQueryBusDecorator implements QueryBus {

    private final QueryBus delegate;
    private final MeterRegistry meterRegistry;

    @Override
    @SuppressWarnings("unchecked")
    public <TResponse> Result<TResponse, Error> dispatch(Query query) {
        var sample = Timer.start();
        var result = (Result<TResponse, Error>) delegate.dispatch(query);
        sample.stop(Timer.builder("query.bus.duration")
                .tag("query", query.getClass().getSimpleName())
                .tag("success", String.valueOf(result.isSuccess()))
                .register(meterRegistry));
        return result;
    }
}
