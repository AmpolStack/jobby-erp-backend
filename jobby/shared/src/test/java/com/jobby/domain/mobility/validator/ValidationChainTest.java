package com.jobby.domain.mobility.validator;

import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Validation Chain Tests")
class ValidationChainTest {

    // ── Core chain behavior ──────────────────────────────────────────
    @Nested
    @DisplayName("Chain creation and build")
    class CoreBehavior {

        @Test
        @DisplayName("empty chain returns success")
        void givenEmptyChain_whenBuild_returnsSuccess() {
            Result<Void, Error> result = ValidationChain.create().build();
            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("all passing returns success")
        void givenAllPassing_whenBuild_returnsSuccess() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateNotNull("value", "field")
                    .validateNotBlank("hello", "field")
                    .build();

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("short-circuits on first failure")
        void givenFirstFails_whenBuild_shortCircuits() {
            var expectedResult = Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("a", "first error"));

            Result<Void, Error> result = ValidationChain.create()
                    .add(() -> expectedResult)
                    .add(() -> Result.failure(ErrorType.VALIDATION_ERROR,
                            new Field("b", "second error")))
                    .build();

            ResultAssertions.assertFailure(result, expectedResult);
        }

        @Test
        @DisplayName("accepts eager Result values")
        void givenEagerResult_whenBuild_returnsSuccess() {
            Result<?, Error> eager = Result.success(null);

            Result<Void, Error> result = ValidationChain.create()
                    .add(eager)
                    .build();

            ResultAssertions.assertSuccess(result, null);
        }
    }

    // ── Null / Blank validations ─────────────────────────────────────
    @Nested
    @DisplayName("validateNotNull()")
    class NotNull {

        @Test
        @DisplayName("passes not null")
        void givenNotNull_whenValidateNotNull_returnsSuccess() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateNotNull("something", "field")
                    .build();

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("fails on null")
        void givenNull_whenValidateNotNull_returnsFailure() {
            var expectedResult = Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("myField", "myField is null"));

            Result<Void, Error> result = ValidationChain.create()
                    .validateNotNull(null, "myField")
                    .build();

            ResultAssertions.assertFailure(result, expectedResult);
        }

    }

    @Nested
    @DisplayName("validateNotBlank()")
    class NotBlank {

        @Test
        @DisplayName("passes non-blank")
        void givenNonBlank_whenValidateNotBlank_returnsSuccess() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateNotBlank("text", "name")
                    .build();

            ResultAssertions.assertSuccess(result);
        }

        @ParameterizedTest(name = "when input is {1}")
        @MethodSource("com.jobby.boundaries.NullityBoundaries#getWithoutNull")
        @DisplayName("fails on blank")
        void givenBlank_whenValidateNotBlank_returnsFailure(String blank, String blankTypeName) {
            var expectedResult = Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("name", "name is blank"));

            Result<Void, Error> result = ValidationChain.create()
                    .validateNotBlank(blank, "name")
                    .build();

            ResultAssertions.assertFailure(result, expectedResult);
        }
    }

    // ── Email validation ─────────────────────────────────────────────
    @Nested
    @DisplayName("validateEmail()")
    class Email {
        @ParameterizedTest(name = "when input is {0}")
        @MethodSource("com.jobby.boundaries.EmailBoundaries#validCases")
        @DisplayName("passes valid email")
        void givenValidEmail_whenValidateEmail_returnsSuccess(String email) {
            Result<Void, Error> result = ValidationChain.create()
                    .validateEmail(email, "email")
                    .build();

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("fails on null")
        void givenNullEmail_whenValidateEmail_returnsFailure() {
            var expectedResult = Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("email", "email is null"));

            Result<Void, Error> result = ValidationChain.create()
                    .validateEmail(null, "email")
                    .build();

            ResultAssertions.assertFailure(result, expectedResult);
        }

        @ParameterizedTest(name = "when input is {1}")
        @MethodSource("com.jobby.boundaries.NullityBoundaries#getWithoutNull")
        @DisplayName("fails on blank")
        void givenBlankEmail_whenValidateEmail_returnsFailure(String blank ,String blankTypeName) {
            var expectedResult = Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("email", "email is blank"));

            Result<Void, Error> result = ValidationChain.create()
                    .validateEmail(blank, "email")
                    .build();

            ResultAssertions.assertFailure(result, expectedResult);
        }

        @ParameterizedTest(name = "when input is {0}")
        @MethodSource("com.jobby.boundaries.EmailBoundaries#invalidFormatCases")
        @DisplayName("fails on invalid format")
        void givenInvalidEmail_whenValidateEmail_returnsFailure(String email) {
            var expectedResult = Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("email","email must be a valid email"));

            Result<Void, Error> result = ValidationChain.create()
                    .validateEmail(email, "email")
                    .build();

            ResultAssertions.assertFailure(result, expectedResult);
        }
    }

    // ── Numeric comparisons ──────────────────────────────────────────
    @Nested
    @DisplayName("Numeric comparison validations")
    class NumericComparisons {

        @ParameterizedTest(name = "when numbers are {0},{1}")
        @CsvSource({"1,0", "2,1", "1000, 999", "10, 5", "50001, 50000", "-1, -2"})
        @DisplayName("passes when value > threshold")
        void givenGreater_whenValidateGreaterThan_returnsSuccess(int a, int b) {
            Result<Void, Error> result = ValidationChain.create()
                    .validateGreaterThan(a, b, "age")
                    .build();

            ResultAssertions.assertSuccess(result);
        }

        @ParameterizedTest(name = "when numbers are {0},{1}")
        @CsvSource({"0,1", "1,2", "999,1000", "5,10", "50000, 50001", "1,1", "50,50", "-2, -1"})
        @DisplayName("fails when value <= threshold")
        void givenLessOrEqual_whenValidateGreaterThan_returnsFailure(int a, int b) {
            var expectedResult = Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("age", "age is less or equals than "+ b));

            Result<Void, Error> result = ValidationChain.create()
                    .validateGreaterThan(a, b, "age")
                    .build();

            ResultAssertions.assertFailure(result, expectedResult);
        }

        @Test
        @DisplayName("passes when value < threshold")
        void givenSmaller_whenValidateSmallerThan_returnsSuccess() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateSmallerThan(3, 5, "qty")
                    .build();

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("fails when value >= threshold")
        void givenGreater_whenValidateSmallerThan_returnsFailure() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateSmallerThan(10, 5, "qty")
                    .build();

            ResultAssertions.assertFailure(result);
        }

        @Test
        @DisplayName("passes when value >= threshold")
        void givenGreater_whenValidateGreaterOrEqualsThan_returnsSuccess() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateGreaterOrEqualsThan(6, 5, "score")
                    .build();

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("passes when value == threshold")
        void givenEqual_whenValidateGreaterOrEqualsThan_returnsSuccess() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateGreaterOrEqualsThan(5, 5, "score")
                    .build();

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("passes when value < threshold")
        void givenSmaller_whenValidateSmallerOrEqualsThan_returnsSuccess() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateSmallerOrEqualsThan(4, 5, "level")
                    .build();

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("passes when value == threshold")
        void givenEqual_whenValidateSmallerOrEqualsThan_returnsSuccess() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateSmallerOrEqualsThan(5, 5, "level")
                    .build();

            ResultAssertions.assertSuccess(result);
        }
    }

    // ── Internal validations ─────────────────────────────────────────

    @Nested
    @DisplayName("Internal validations")
    class InternalValidations {

        @Test
        @DisplayName("fails with ITN_VALIDATION_NULL")
        void givenNull_whenValidateInternalNotNull_returnsFailure() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalNotNull(null, "field")
                    .build();

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_NULL);
        }

        @Test
        @DisplayName("fails with ITN_VALIDATION_BLANK")
        void givenBlank_whenValidateInternalNotBlank_returnsFailure() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalNotBlank("  ", "field")
                    .build();

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_BLANK);
        }

        @Test
        @DisplayName("fails with ITN_VALIDATION_NULL")
        void givenNull_whenValidateInternalNotBlank_returnsFailure() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalNotBlank(null, "field")
                    .build();

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_NULL);
        }

        @Test
        @DisplayName("passes valid email")
        void givenValidEmail_whenValidateInternalEmail_returnsSuccess() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalEmail("admin@test.co", "email")
                    .build();

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("fails with ITN_VALIDATION_FORMAT")
        void givenInvalidEmail_whenValidateInternalEmail_returnsFailure() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalEmail("invalid", "email")
                    .build();

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_FORMAT);
        }

        @Test
        @DisplayName("fails with ITN_VALIDATION_RANGE (int)")
        void givenLess_whenValidateInternalGreaterThanInt_returnsFailure() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalGreaterThan(1, 5, "qty")
                    .build();

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_RANGE);
        }

        @Test
        @DisplayName("fails with ITN_VALIDATION_RANGE (long)")
        void givenLess_whenValidateInternalGreaterThanLong_returnsFailure() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalGreaterThan(1L, 5L, "id")
                    .build();

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_RANGE);
        }

        @Test
        @DisplayName("fails with ITN_VALIDATION_RANGE")
        void givenGreater_whenValidateInternalSmallerThan_returnsFailure() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalSmallerThan(10, 5, "max")
                    .build();

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_RANGE);
        }

        @Test
        @DisplayName("passes when value == threshold")
        void givenEqual_whenValidateInternalGreaterOrEqualsThan_returnsSuccess() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalGreaterOrEqualsThan(5, 5, "val")
                    .build();

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("passes when value == threshold")
        void givenEqual_whenValidateInternalSmallerOrEqualsThan_returnsSuccess() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalSmallerOrEqualsThan(5, 5, "val")
                    .build();

            ResultAssertions.assertSuccess(result);
        }
    }

    // ── AnyMatch ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("validateInternalAnyMatch()")
    class AnyMatch {

        @Test
        @DisplayName("passes on match")
        void givenMatching_whenValidateInternalAnyMatch_returnsSuccess() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalAnyMatch("B", new String[] { "A", "B", "C" }, "option")
                    .build();

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("fails on no match")
        void givenNonMatching_whenValidateInternalAnyMatch_returnsFailure() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalAnyMatch("Z", new String[] { "A", "B", "C" }, "option")
                    .build();

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
        }
    }

    // ── Custom validations ───────────────────────────────────────────

    @Nested
    @DisplayName("Custom validations")
    class CustomValidations {

        @Test
        @DisplayName("passes when true")
        void givenTrue_whenValidateCustom_returnsSuccess() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateCustom(true, "field", "must be true")
                    .build();

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("fails when false")
        void givenFalse_whenValidateCustom_returnsFailure() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateCustom(false, "field", "custom msg")
                    .build();

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.VALIDATION_ERROR);
            assertThat(result.error().getFields()[0].getReason()).isEqualTo("custom msg");
        }

        @Test
        @DisplayName("fails with ITN_VALIDATION_CUSTOM")
        void givenFalse_whenValidateInternalCustom_returnsFailure() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalCustom(false, "field", "bad state")
                    .build();

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_CUSTOM);
            assertThat(result.error().getFields()[0].getReason())
                    .contains("Internal validation failed");
        }
    }

    // ── Conditional validations ──────────────────────────────────────

    @Nested
    @DisplayName("validateIf()")
    class ConditionalValidation {

        @Test
        @DisplayName("executes when true")
        void givenTrue_whenValidateIf_executesValidation() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateIf(true, () -> Result.failure(
                            ErrorType.VALIDATION_ERROR, new Field("f", "triggered")))
                    .build();

            ResultAssertions.assertFailure(result);
        }

        @Test
        @DisplayName("skips when false")
        void givenFalse_whenValidateIf_skipsValidation() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateIf(false, () -> Result.failure(
                            ErrorType.VALIDATION_ERROR, new Field("f", "triggered")))
                    .build();

            ResultAssertions.assertSuccess(result);
        }
    }

    // ── Chaining multiple validations ────────────────────────────────

    @Nested
    @DisplayName("Full chain scenarios")
    class FullChainScenarios {

        @Test
        @DisplayName("all valid pass")
        void givenAllValid_whenChained_returnsSuccess() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateNotNull("John", "firstName")
                    .validateNotBlank("john@test.com", "email")
                    .validateEmail("john@test.com", "email")
                    .validateGreaterThan(25, 18, "age")
                    .build();

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("stops at first failure")
        void givenFirstFails_whenChained_stopsAtFirstFailure() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateNotNull("John", "firstName")
                    .validateNotBlank("", "lastName")
                    .validateEmail("invalid", "email")
                    .build();

            ResultAssertions.assertFailure(result);
            assertThat(result.error().getFields()[0].getInstance()).isEqualTo("lastName");
        }
    }
}
