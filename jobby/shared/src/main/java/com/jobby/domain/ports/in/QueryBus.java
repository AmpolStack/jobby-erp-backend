package com.jobby.domain.ports.in;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;

public interface QueryBus {
    <TResponse> Result<TResponse, Error> dispatch(Query query);
}
