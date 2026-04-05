package com.jobby.domain.mobility.result;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.exceptions.InconsistencyResultException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResultTest {

    // ── Factory methods ──────────────────────────────────────────────

    @Nested
    @DisplayName("Factory: success()")
    class SuccessFactory {

        @Test
        @DisplayName("creates a successful result with data")
        void createsSuccessWithData() {
            Result<String, String> result = Result.success("hello");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isFailure()).isFalse();
            assertThat(result.data()).isEqualTo("hello");
            assertThat(result.error()).isNull();
        }

        @Test
        @DisplayName("creates a successful result with null data")
        void createsSuccessWithNullData() {
            Result<Void, String> result = Result.success(null);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.data()).isNull();
        }
    }

    @Nested
    @DisplayName("Factory: failure()")
    class FailureFactory {

        @Test
        @DisplayName("creates a failed result with a generic error")
        void createsFailureWithGenericError() {
            Result<String, String> result = Result.failure("something went wrong");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.isFailure()).isTrue();
            assertThat(result.data()).isNull();
            assertThat(result.error()).isEqualTo("something went wrong");
        }

        @Test
        @DisplayName("creates a failed result from ErrorType + Field array")
        void createsFailureWithErrorTypeAndFieldArray() {
            Field[] fields = { new Field("name", "name is null") };
            Result<String, Error> result = Result.failure(ErrorType.VALIDATION_ERROR, fields);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.VALIDATION_ERROR);
            assertThat(result.error().getFields()).hasSize(1);
            assertThat(result.error().getFields()[0].getInstance()).isEqualTo("name");
        }

        @Test
        @DisplayName("creates a failed result from ErrorType + single Field")
        void createsFailureWithErrorTypeAndSingleField() {
            Result<Integer, Error> result = Result.failure(
                    ErrorType.NOT_FOUND, new Field("id", "entity not found"));

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.NOT_FOUND);
            assertThat(result.error().getFields()[0].getReason()).isEqualTo("entity not found");
        }
    }

    // ── Propagation helpers ──────────────────────────────────────────

    @Nested
    @DisplayName("propagateFailure()")
    class PropagateFailure {

        @Test
        @DisplayName("propagates the error to a new result type")
        void propagatesErrorTypeChange() {
            Result<String, String> original = Result.failure("db error");
            Result<Integer, String> propagated = Result.propagateFailure(original);

            assertThat(propagated.isFailure()).isTrue();
            assertThat(propagated.error()).isEqualTo("db error");
        }

        @Test
        @DisplayName("propagates error replacing field name for non-ITN errors")
        void propagatesReplacingFieldNameForNonItnErrors() {
            Field[] fields = { new Field("oldField", "value is invalid") };
            Result<String, Error> original = Result.failure(
                    ErrorType.VALIDATION_ERROR, fields);

            Result<Integer, Error> propagated = Result.propagateFailure(original, "newField");

            assertThat(propagated.isFailure()).isTrue();
            assertThat(propagated.error().getFields()[0].getInstance()).isEqualTo("newField");
            assertThat(propagated.error().getFields()[0].getReason()).isEqualTo("value is invalid");
        }

        @Test
        @DisplayName("propagates error keeping original field name for ITN errors")
        void propagatesKeepingFieldNameForItnErrors() {
            Field[] fields = { new Field("internalField", "some reason") };
            Result<String, Error> original = Result.failure(
                    ErrorType.ITN_VALIDATION_NULL, fields);

            Result<Integer, Error> propagated = Result.propagateFailure(original, "newField");

            assertThat(propagated.isFailure()).isTrue();
            assertThat(propagated.error().getFields()[0].getInstance()).isEqualTo("internalField");
        }
    }

    // ── Transformations ──────────────────────────────────────────────

    @Nested
    @DisplayName("map()")
    class Map {

        @Test
        @DisplayName("transforms data on success")
        void transformsDataOnSuccess() {
            Result<Integer, String> result = Result.<Integer, String>success(5)
                    .map(v -> v * 2);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.data()).isEqualTo(10);
        }

        @Test
        @DisplayName("skips transformation on failure")
        void skipsTransformationOnFailure() {
            Result<Integer, String> result = Result.<Integer, String>failure("error")
                    .map(v -> v * 2);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error()).isEqualTo("error");
        }
    }

    @Nested
    @DisplayName("flatMap()")
    class FlatMap {

        @Test
        @DisplayName("chains successfully when inner result is success")
        void chainsSuccessfully() {
            Result<Integer, String> result = Result.<Integer, String>success(5)
                    .flatMap(v -> Result.success(v + 10));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.data()).isEqualTo(15);
        }

        @Test
        @DisplayName("short-circuits when outer result is failure")
        void shortCircuitsOnOuterFailure() {
            Result<Integer, String> result = Result.<Integer, String>failure("error")
                    .flatMap(v -> Result.success(v + 10));

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error()).isEqualTo("error");
        }

        @Test
        @DisplayName("propagates inner failure")
        void propagatesInnerFailure() {
            Result<Integer, String> result = Result.<Integer, String>success(5)
                    .flatMap(v -> Result.failure("inner error"));

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error()).isEqualTo("inner error");
        }
    }

    @Nested
    @DisplayName("fold()")
    class Fold {

        @Test
        @DisplayName("executes onSuccess consumer on success")
        void executesOnSuccessConsumer() {
            StringBuilder sb = new StringBuilder();
            Result.<String, String>success("data")
                    .fold(sb::append, err -> sb.append("FAIL"));

            assertThat(sb.toString()).isEqualTo("data");
        }

        @Test
        @DisplayName("executes onFailure consumer on failure")
        void executesOnFailureConsumer() {
            StringBuilder sb = new StringBuilder();
            Result.<String, String>failure("oops")
                    .fold(data -> sb.append("OK"), sb::append);

            assertThat(sb.toString()).isEqualTo("oops");
        }
    }

    // ── mapError ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("mapError()")
    class MapError {

        @Test
        @DisplayName("converts a failure to a different data type")
        void convertsFailureToDifferentDataType() {
            Result<String, String> original = Result.failure("err");
            Result<Integer, String> mapped = Result.mapError(original);

            assertThat(mapped.isFailure()).isTrue();
            assertThat(mapped.error()).isEqualTo("err");
        }

        @Test
        @DisplayName("throws InconsistencyResultException when result is success")
        void throwsOnSuccess() {
            Result<String, String> original = Result.success("data");

            assertThatThrownBy(() -> Result.mapError(original))
                    .isInstanceOf(InconsistencyResultException.class);
        }
    }

    // ── Equality ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two successes with same data are equal")
        void successEquality() {
            Result<String, String> a = Result.success("x");
            Result<String, String> b = Result.success("x");

            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("two failures with same error are equal")
        void failureEquality() {
            Result<String, String> a = Result.failure("e");
            Result<String, String> b = Result.failure("e");

            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("success and failure are not equal")
        void successAndFailureAreNotEqual() {
            Result<String, String> success = Result.success("x");
            Result<String, String> failure = Result.failure("x");

            assertThat(success).isNotEqualTo(failure);
        }
    }
}
