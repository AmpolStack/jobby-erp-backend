package com.jobby.infrastructure.bus;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.in.Command;
import com.jobby.domain.ports.in.CommandBus;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MetricsCommandBusDecorator implements CommandBus {

    private final CommandBus delegate;
    private final MeterRegistry meterRegistry;

    @Override
    @SuppressWarnings("unchecked")
    public <TResponse> Result<TResponse, Error> dispatch(Command command) {
        var sample = Timer.start();
        var result = (Result<TResponse, Error>) delegate.dispatch(command);
        sample.stop(Timer.builder("command.bus.duration")
                .tag("command", command.getClass().getSimpleName())
                .tag("success", String.valueOf(result.isSuccess()))
                .register(meterRegistry));
        return result;
    }
}
