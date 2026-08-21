package com.jobby.domain.ports.in;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;

public interface CommandBus {
    <TResponse> Result<TResponse, Error> dispatch(Command command);
}
