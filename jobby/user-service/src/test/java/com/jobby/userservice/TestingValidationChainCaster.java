package com.jobby.userservice;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;

public final class TestingValidationChainCaster {
    public static Result<Void, Error> buildExpectedError(TestingValidationType type, String fieldName) {
        return switch (type) {
            case NOT_BLANK -> ValidationChain.create().validateNotBlank(null, fieldName).build();
            case NOT_NULL -> ValidationChain.create().validateNotNull(null, fieldName).build();
            case INTERNAL_NOT_NULL -> ValidationChain.create().validateInternalNotNull(null, fieldName).build();
        };
    }
}
