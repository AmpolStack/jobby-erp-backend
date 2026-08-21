package com.jobby.domain.ports.in;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;

@FunctionalInterface
public interface QueryHandler<TQuery extends Query, TResponse> {
    Result<TResponse, Error> execute(TQuery query);
}
