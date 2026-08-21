package com.jobby.infrastructure.adapter;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.OptionalOperation;
import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
public class OptionalOperationAdapter implements OptionalOperation {

    @Override
    public <T> Optional<T> run(Result<T, Error> result) {
        if (result.isFailure()) {
            var error = result.error();
            var type = error.getCode();

            if (type.toString().startsWith("ITS_")) {
                log.error("[OPTIONAL FAILED] type={} fields={}",
                        type, Arrays.toString(error.getFields()));
            } else {
                log.warn("[OPTIONAL FAILED] type={} fields={}",
                        type, Arrays.toString(error.getFields()));
            }

            return Optional.empty();
        }

        return Optional.ofNullable(result.data());
    }
}
