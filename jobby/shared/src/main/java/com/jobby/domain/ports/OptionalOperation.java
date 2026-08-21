package com.jobby.domain.ports;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import java.util.Optional;

public interface OptionalOperation {
    <T> Optional<T> run(Result<T, Error> result);
}
