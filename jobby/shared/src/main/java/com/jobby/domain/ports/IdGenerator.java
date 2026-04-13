package com.jobby.domain.ports;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;

public interface IdGenerator {
    Result<Long, Error> next();
}
