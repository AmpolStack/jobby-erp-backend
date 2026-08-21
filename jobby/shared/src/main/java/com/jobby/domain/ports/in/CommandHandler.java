package com.jobby.domain.ports.in;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;

@FunctionalInterface
public interface CommandHandler<TCommand extends Command, TResponse> {
    Result<TResponse, Error> execute(TCommand command);
}
