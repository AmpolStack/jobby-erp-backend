package com.jobby.domain.ports.in;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;

@FunctionalInterface
public interface EventHandler<TEvent extends Event, TResponse> {
    Result<TResponse, Error> execute(TEvent event);
}
