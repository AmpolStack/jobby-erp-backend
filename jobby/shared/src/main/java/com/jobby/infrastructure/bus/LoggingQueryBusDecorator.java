package com.jobby.infrastructure.bus;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.in.Query;
import com.jobby.domain.ports.in.QueryBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LoggingQueryBusDecorator implements QueryBus {

    private final QueryBus delegate;

    @Override
    @SuppressWarnings("unchecked")
    public <TResponse> Result<TResponse, Error> dispatch(Query query) {
        var start = System.currentTimeMillis();
        log.info("→ Dispatching query: {}", query.getClass().getSimpleName());

        var result = (Result<TResponse, Error>) delegate.dispatch(query);

        var elapsed = System.currentTimeMillis() - start;
        if (result.isSuccess()) {
            log.info("← {} succeeded ({}ms)", query.getClass().getSimpleName(), elapsed);
        } else {
            log.warn("← {} failed ({}ms): {}", query.getClass().getSimpleName(), elapsed, result.error());
        }
        return result;
    }
}
