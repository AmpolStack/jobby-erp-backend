package com.jobby.infrastructure.bus;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.in.Event;
import com.jobby.domain.ports.in.EventBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LoggingEventBusDecorator implements EventBus {

    private final EventBus delegate;

    @Override
    @SuppressWarnings("unchecked")
    public <TResponse> Result<TResponse, Error> dispatch(Event event) {
        var start = System.currentTimeMillis();
        log.info("-> Dispatching event: {}", event.getClass().getSimpleName());
        var result = (Result<TResponse, Error>) delegate.dispatch(event);
        var elapsed = System.currentTimeMillis() - start;
        if (result.isSuccess()) {
            log.info("<- {} succeeded ({}ms)", event.getClass().getSimpleName(), elapsed);
        } else {
            log.warn("<- {} failed ({}ms): {}", event.getClass().getSimpleName(), elapsed, result.error());
        }
        return result;
    }
}
