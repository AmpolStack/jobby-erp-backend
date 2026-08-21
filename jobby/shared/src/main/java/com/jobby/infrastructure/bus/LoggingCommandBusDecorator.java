package com.jobby.infrastructure.bus;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.in.Command;
import com.jobby.domain.ports.in.CommandBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LoggingCommandBusDecorator implements CommandBus {

    private final CommandBus delegate;

    @Override
    @SuppressWarnings("unchecked")
    public <TResponse> Result<TResponse, Error> dispatch(Command command) {
        var start = System.currentTimeMillis();
        log.info("→ Dispatching command: {}", command.getClass().getSimpleName());

        var result = (Result<TResponse, Error>) delegate.dispatch(command);

        var elapsed = System.currentTimeMillis() - start;
        if (result.isSuccess()) {
            log.info("← {} succeeded ({}ms)", command.getClass().getSimpleName(), elapsed);
        } else {
            log.warn("← {} failed ({}ms): {}", command.getClass().getSimpleName(), elapsed, result.error());
        }
        return result;
    }
}
