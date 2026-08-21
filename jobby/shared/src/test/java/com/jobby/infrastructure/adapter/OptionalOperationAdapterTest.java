package com.jobby.infrastructure.adapter;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OptionalOperationAdapter - Unit Tests")
class OptionalOperationAdapterTest {

    private OptionalOperationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OptionalOperationAdapter();
    }

    @Test
    @DisplayName("run: success with non-null data returns Optional with value")
    void givenSuccessResultWithData_whenRun_returnsOptionalWithValue() {
        Result<String, Error> result = Result.success("hello");

        var optional = adapter.run(result);

        assertThat(optional).isPresent();
        assertThat(optional.get()).isEqualTo("hello");
    }

    @Test
    @DisplayName("run: success with null data returns Optional.empty")
    void givenSuccessResultWithNullData_whenRun_returnsEmptyOptional() {
        Result<String, Error> result = Result.success(null);

        var optional = adapter.run(result);

        assertThat(optional).isEmpty();
    }

    @Test
    @DisplayName("run: ITS_ system error returns Optional.empty")
    void givenItsFailure_whenRun_returnsEmptyOptional() {
        var result = Result.failure(ErrorType.ITS_SERIALIZATION_ERROR,
                new Field("serialization", "failed"));

        var optional = adapter.run(result);

        assertThat(optional).isEmpty();
    }

    @Test
    @DisplayName("run: non-ITS error returns Optional.empty")
    void givenNonItsFailure_whenRun_returnsEmptyOptional() {
        var result = Result.failure(ErrorType.VALIDATION_ERROR,
                new Field("email", "invalid format"));

        var optional = adapter.run(result);

        assertThat(optional).isEmpty();
    }

    @Test
    @DisplayName("run: ITN_ internal validation error returns Optional.empty")
    void givenItnFailure_whenRun_returnsEmptyOptional() {
        var result = Result.failure(ErrorType.ITN_VALIDATION_BLANK,
                new Field("name", "must not be blank"));

        var optional = adapter.run(result);

        assertThat(optional).isEmpty();
    }
}
