package com.jobby.infrastructure.transaction;

import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SpringDataTransactionalContext - Unit Tests")
class SpringDataTransactionalContextTest {

    private final SpringDataTransactionalContext context = new SpringDataTransactionalContext();

    @Test
    @DisplayName("run: success supplier returns success")
    void givenSuccessSupplier_whenRun_returnsSuccess() {
        var result = context.run(() -> Result.success("data"));

        ResultAssertions.assertSuccess(result);
    }

    @Test
    @DisplayName("runReadOnly: success supplier returns success with data")
    void givenSuccessSupplier_whenRunReadOnly_returnsSuccessWithData() {
        var result = context.runReadOnly(() -> Result.success(42));

        ResultAssertions.assertSuccess(result);
        assert result.data() == 42;
    }

    @Test
    @DisplayName("runReadOnly: failure supplier returns failure")
    void givenFailureSupplier_whenRunReadOnly_returnsFailure() {
        var result = context.runReadOnly(() ->
                Result.failure(ErrorType.VALIDATION_ERROR, new Field("field", "error message")));

        ResultAssertions.assertFailure(result);
    }
}
