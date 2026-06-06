package com.jobby.domain.mobility.result;

import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.exceptions.InconsistencyResultException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Result Pattern Tests")
class ResultTest {

    // ── Factory methods ──────────────────────────────────────────────
    @Nested
    @DisplayName("Factory: success()")
    class SuccessFactory {

        @Test
        @DisplayName("success with data")
        void givenData_whenSuccess_returnsSuccessWithData() {
            Result<String, String> result = Result.success("hello");

            ResultAssertions.assertSuccess(result);
            assertThat(result.data()).isEqualTo("hello");
            assertThat(result.error()).isNull();
        }

        @Test
        @DisplayName("success with null data")
        void givenNullData_whenSuccess_returnsSuccessWithNullData() {
            Result<Void, String> result = Result.success(null);

            ResultAssertions.assertSuccess(result);
            assertThat(result.data()).isNull();
        }
    }

    @Nested
    @DisplayName("Factory: failure()")
    class FailureFactory {

        @Test
        @DisplayName("failure with error")
        void givenError_whenFailure_returnsFailureWithError() {
            Result<String, String> result = Result.failure("something went wrong");

            ResultAssertions.assertFailure(result);
            assertThat(result.data()).isNull();
            assertThat(result.error()).isEqualTo("something went wrong");
        }

        @Test
        @DisplayName("failure with ErrorType and fields")
        void givenErrorTypeAndFields_whenFailure_returnsFailureWithError() {
            Field[] fields = { new Field("name", "name is null") };
            Result<String, Error> result = Result.failure(ErrorType.VALIDATION_ERROR, fields);

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.VALIDATION_ERROR);
            assertThat(result.error().getFields()).hasSize(1);
            assertThat(result.error().getFields()[0].getInstance()).isEqualTo("name");
        }

        @Test
        @DisplayName("failure with ErrorType and single field")
        void givenErrorTypeAndField_whenFailure_returnsFailureWithError() {
            Result<Integer, Error> result = Result.failure(
                    ErrorType.NOT_FOUND, new Field("id", "entity not found"));

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.NOT_FOUND);
            assertThat(result.error().getFields()[0].getReason()).isEqualTo("entity not found");
        }
    }

    // ── Propagation helpers ──────────────────────────────────────────

    @Nested
    @DisplayName("propagateFailure()")
    class PropagateFailure {

        @Test
        @DisplayName("propagates error")
        void givenError_whenPropagateFailure_propagatesError() {
            Result<String, String> original = Result.failure("db error");
            Result<Integer, String> propagated = Result.propagateFailure(original);

            ResultAssertions.assertFailure(propagated);
            assertThat(propagated.error()).isEqualTo("db error");
        }

        @Test
        @DisplayName("replaces field name for non-ITN errors")
        void givenNonItnError_whenPropagateFailure_replacesFieldName() {
            Field[] fields = { new Field("oldField", "value is invalid") };
            Result<String, Error> original = Result.failure(
                    ErrorType.VALIDATION_ERROR, fields);

            Result<Integer, Error> propagated = Result.propagateFailure(original, "newField");

            ResultAssertions.assertFailure(propagated);
            assertThat(propagated.error().getFields()[0].getInstance()).isEqualTo("newField");
            assertThat(propagated.error().getFields()[0].getReason()).isEqualTo("value is invalid");
        }

        @Test
        @DisplayName("keeps original field name for ITN errors")
        void givenItnError_whenPropagateFailure_keepsFieldName() {
            Field[] fields = { new Field("internalField", "some reason") };
            Result<String, Error> original = Result.failure(
                    ErrorType.ITN_VALIDATION_NULL, fields);

            Result<Integer, Error> propagated = Result.propagateFailure(original, "newField");

            ResultAssertions.assertFailure(propagated);
            assertThat(propagated.error().getFields()[0].getInstance()).isEqualTo("internalField");
        }
    }

    // ── Transformations ──────────────────────────────────────────────

    @Nested
    @DisplayName("map()")
    class Map {

        @Test
        @DisplayName("transforms data on success")
        void givenSuccess_whenMap_transformsData() {
            Result<Integer, String> result = Result.<Integer, String>success(5)
                    .map(v -> v * 2);

            ResultAssertions.assertSuccess(result);
            assertThat(result.data()).isEqualTo(10);
        }

        @Test
        @DisplayName("skips transformation on failure")
        void givenFailure_whenMap_skipsTransformation() {
            Result<Integer, String> result = Result.<Integer, String>failure("error")
                    .map(v -> v * 2);

            ResultAssertions.assertFailure(result);
            assertThat(result.error()).isEqualTo("error");
        }
    }

    @Nested
    @DisplayName("flatMap()")
    class FlatMap {

        @Test
        @DisplayName("chains successfully")
        void givenSuccess_whenFlatMap_chainsSuccessfully() {
            Result<Integer, String> result = Result.<Integer, String>success(5)
                    .flatMap(v -> Result.success(v + 10));

            ResultAssertions.assertSuccess(result);
            assertThat(result.data()).isEqualTo(15);
        }

        @Test
        @DisplayName("short-circuits on outer failure")
        void givenOuterFailure_whenFlatMap_shortCircuits() {
            Result<Integer, String> result = Result.<Integer, String>failure("error")
                    .flatMap(v -> Result.success(v + 10));

            ResultAssertions.assertFailure(result);
            assertThat(result.error()).isEqualTo("error");
        }

        @Test
        @DisplayName("propagates inner failure")
        void givenInnerFailure_whenFlatMap_propagatesFailure() {
            Result<Integer, String> result = Result.<Integer, String>success(5)
                    .flatMap(v -> Result.failure("inner error"));

            ResultAssertions.assertFailure(result);
            assertThat(result.error()).isEqualTo("inner error");
        }
    }

    @Nested
    @DisplayName("fold()")
    class Fold {

        @Test
        @DisplayName("executes onSuccess consumer")
        void givenSuccess_whenFold_executesOnSuccess() {
            StringBuilder sb = new StringBuilder();
            Result.<String, String>success("data")
                    .fold(sb::append, err -> {
                        sb.append("FAIL");
                    });

            assertThat(sb.toString()).isEqualTo("data");
        }

        @Test
        @DisplayName("executes onFailure consumer")
        void givenFailure_whenFold_executesOnFailure() {
            StringBuilder sb = new StringBuilder();
            Result.<String, String>failure("oops")
                    .fold(data -> sb.append("OK"), err -> {
                        sb.append(err);
                    });

            assertThat(sb.toString()).isEqualTo("oops");
        }
    }

    // ── mapError ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("mapError()")
    class MapError {

        @Test
        @DisplayName("converts failure data type")
        void givenFailure_whenMapError_convertsDataType() {
            Result<String, String> original = Result.failure("err");
            Result<Integer, String> mapped = Result.mapError(original);

            ResultAssertions.assertFailure(mapped);
            assertThat(mapped.error()).isEqualTo("err");
        }

        @Test
        @DisplayName("throws on success")
        void givenSuccess_whenMapError_throwsException() {
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
        @DisplayName("successes with same data are equal")
        void givenSameData_whenSuccess_areEqual() {
            Result<String, String> a = Result.success("x");
            Result<String, String> b = Result.success("x");

            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("failures with same error are equal")
        void givenSameError_whenFailure_areEqual() {
            Result<String, String> a = Result.failure("e");
            Result<String, String> b = Result.failure("e");

            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("success and failure are not equal")
        void givenSuccessAndFailure_areNotEqual() {
            Result<String, String> success = Result.success("x");
            Result<String, String> failure = Result.failure("x");

            assertThat(success).isNotEqualTo(failure);
        }
    }
}
