package com.jobby.domain.mobility.validator;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationChainTest {

    // ── Core chain behavior ──────────────────────────────────────────

    @Nested
    @DisplayName("Chain creation and build")
    class CoreBehavior {

        @Test
        @DisplayName("empty chain builds to success")
        void emptyChainBuildsToSuccess() {
            Result<Void, Error> result = ValidationChain.create().build();

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("chain with all passing validations builds to success")
        void allPassingValidationsReturnSuccess() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateNotNull("value", "field")
                    .validateNotBlank("hello", "field")
                    .build();

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("short-circuits on first failure (lazy supplier)")
        void shortCircuitsOnFirstFailure() {
            Result<Void, Error> result = ValidationChain.create()
                    .add(() -> Result.failure(ErrorType.VALIDATION_ERROR,
                            new Field("a", "first error")))
                    .add(() -> Result.failure(ErrorType.VALIDATION_ERROR,
                            new Field("b", "second error")))
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getFields()[0].getInstance()).isEqualTo("a");
        }

        @Test
        @DisplayName("accepts eager Result values via add(Result)")
        void acceptsEagerResultValues() {
            Result<?, Error> eager = Result.success(null);

            Result<Void, Error> result = ValidationChain.create()
                    .add(eager)
                    .build();

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ── Null / Blank validations ─────────────────────────────────────

    @Nested
    @DisplayName("validateNotNull()")
    class NotNull {

        @Test
        @DisplayName("passes when value is not null")
        void passesNotNull() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateNotNull("something", "field")
                    .build();

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("fails when value is null")
        void failsOnNull() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateNotNull(null, "myField")
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.VALIDATION_ERROR);
            assertThat(result.error().getFields()[0].getInstance()).isEqualTo("myField");
        }
    }

    @Nested
    @DisplayName("validateNotBlank()")
    class NotBlank {

        @Test
        @DisplayName("passes with non-blank string")
        void passesNonBlank() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateNotBlank("text", "name")
                    .build();

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("fails when string is null")
        void failsOnNull() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateNotBlank(null, "name")
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getFields()[0].getReason()).contains("is null");
        }

        @Test
        @DisplayName("fails when string is blank")
        void failsOnBlank() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateNotBlank("   ", "name")
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getFields()[0].getReason()).contains("is blank");
        }
    }

    // ── Email validation ─────────────────────────────────────────────

    @Nested
    @DisplayName("validateEmail()")
    class Email {

        @Test
        @DisplayName("passes with valid email")
        void passesWithValidEmail() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateEmail("user@example.com", "email")
                    .build();

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("fails when email is null")
        void failsOnNull() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateEmail(null, "email")
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getFields()[0].getReason()).contains("is null");
        }

        @Test
        @DisplayName("fails when email is blank")
        void failsOnBlank() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateEmail("  ", "email")
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getFields()[0].getReason()).contains("is blank");
        }

        @Test
        @DisplayName("fails when email format is invalid")
        void failsInvalidFormat() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateEmail("not-an-email", "email")
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getFields()[0].getReason()).contains("valid email");
        }
    }

    // ── Numeric comparisons ──────────────────────────────────────────

    @Nested
    @DisplayName("Numeric comparison validations")
    class NumericComparisons {

        @Test
        @DisplayName("validateGreaterThan passes when value >= threshold")
        void greaterThanPasses() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateGreaterThan(10, 5, "age")
                    .build();

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("validateGreaterThan fails when value < threshold")
        void greaterThanFails() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateGreaterThan(3, 5, "age")
                    .build();

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("validateSmallerThan passes when value <= threshold")
        void smallerThanPasses() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateSmallerThan(3, 5, "qty")
                    .build();

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("validateSmallerThan fails when value > threshold")
        void smallerThanFails() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateSmallerThan(10, 5, "qty")
                    .build();

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("validateGreaterOrEqualsThan passes when value > threshold")
        void greaterOrEqualsPasses() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateGreaterOrEqualsThan(6, 5, "score")
                    .build();

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("validateGreaterOrEqualsThan fails when value <= threshold")
        void greaterOrEqualsFails() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateGreaterOrEqualsThan(5, 5, "score")
                    .build();

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("validateSmallerOrEqualsThan passes when value < threshold")
        void smallerOrEqualsPasses() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateSmallerOrEqualsThan(4, 5, "level")
                    .build();

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("validateSmallerOrEqualsThan fails when value >= threshold")
        void smallerOrEqualsFails() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateSmallerOrEqualsThan(5, 5, "level")
                    .build();

            assertThat(result.isFailure()).isTrue();
        }
    }

    // ── Internal validations ─────────────────────────────────────────

    @Nested
    @DisplayName("Internal validations")
    class InternalValidations {

        @Test
        @DisplayName("validateInternalNotNull fails with ITN_VALIDATION_NULL")
        void internalNotNullFails() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalNotNull(null, "field")
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_NULL);
        }

        @Test
        @DisplayName("validateInternalNotBlank fails with ITN_VALIDATION_BLANK for blank")
        void internalNotBlankFails() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalNotBlank("  ", "field")
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_BLANK);
        }

        @Test
        @DisplayName("validateInternalNotBlank fails with ITN_VALIDATION_NULL for null")
        void internalNotBlankNullFails() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalNotBlank(null, "field")
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_NULL);
        }

        @Test
        @DisplayName("validateInternalEmail passes with valid email")
        void internalEmailPasses() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalEmail("admin@test.co", "email")
                    .build();

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("validateInternalEmail fails with ITN_VALIDATION_FORMAT")
        void internalEmailInvalidFormat() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalEmail("invalid", "email")
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_FORMAT);
        }

        @Test
        @DisplayName("validateInternalGreaterThan (int) fails with ITN_VALIDATION_RANGE")
        void internalGreaterThanIntFails() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalGreaterThan(1, 5, "qty")
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_RANGE);
        }

        @Test
        @DisplayName("validateInternalGreaterThan (long) fails with ITN_VALIDATION_RANGE")
        void internalGreaterThanLongFails() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalGreaterThan(1L, 5L, "id")
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_RANGE);
        }

        @Test
        @DisplayName("validateInternalSmallerThan fails with ITN_VALIDATION_RANGE")
        void internalSmallerThanFails() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalSmallerThan(10, 5, "max")
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_RANGE);
        }

        @Test
        @DisplayName("validateInternalGreaterOrEqualsThan fails with ITN_VALIDATION_RANGE")
        void internalGreaterOrEqualsFails() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalGreaterOrEqualsThan(5, 5, "val")
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_RANGE);
        }

        @Test
        @DisplayName("validateInternalSmallerOrEqualsThan fails with ITN_VALIDATION_RANGE")
        void internalSmallerOrEqualsFails() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalSmallerOrEqualsThan(5, 5, "val")
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITN_VALIDATION_RANGE);
        }
    }

    // ── AnyMatch ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("validateInternalAnyMatch()")
    class AnyMatch {

        @Test
        @DisplayName("passes when value matches one of the options")
        void passesOnMatch() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalAnyMatch("B", new String[] { "A", "B", "C" }, "option")
                    .build();

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("fails when value matches none of the options")
        void failsOnNoMatch() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalAnyMatch("Z", new String[] { "A", "B", "C" }, "option")
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.ITS_INVALID_OPTION_PARAMETER);
        }
    }

    // ── Custom validations ───────────────────────────────────────────

    @Nested
    @DisplayName("Custom validations")
    class CustomValidations {

        @Test
        @DisplayName("validateCustom passes when condition is true")
        void customPasses() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateCustom(true, "field", "must be true")
                    .build();

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("validateCustom fails when condition is false")
        void customFails() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateCustom(false, "field", "custom msg")
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getCode()).isEqualTo(ErrorType.VALIDATION_ERROR);
            assertThat(result.error().getFields()[0].getReason()).isEqualTo("custom msg");
        }

        @Test
        @DisplayName("validateInternalCustom fails with ITN_VALIDATION_CUSTOM")
        void internalCustomFails() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateInternalCustom(false, "field", "bad state")
                    .build();

            assertThat(result.isFailure()).isTrue();
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
        @DisplayName("executes validation when condition is true")
        void executesWhenTrue() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateIf(true, () -> Result.failure(
                            ErrorType.VALIDATION_ERROR, new Field("f", "triggered")))
                    .build();

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("skips validation when condition is false")
        void skipsWhenFalse() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateIf(false, () -> Result.failure(
                            ErrorType.VALIDATION_ERROR, new Field("f", "triggered")))
                    .build();

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ── Chaining multiple validations ────────────────────────────────

    @Nested
    @DisplayName("Full chain scenarios")
    class FullChainScenarios {

        @Test
        @DisplayName("multiple validations all pass")
        void multipleValidationsAllPass() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateNotNull("John", "firstName")
                    .validateNotBlank("john@test.com", "email")
                    .validateEmail("john@test.com", "email")
                    .validateGreaterThan(25, 18, "age")
                    .build();

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("chain stops at first validation failure")
        void chainStopsAtFirstFailure() {
            Result<Void, Error> result = ValidationChain.create()
                    .validateNotNull("John", "firstName")
                    .validateNotBlank("", "lastName")
                    .validateEmail("invalid", "email")
                    .build();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.error().getFields()[0].getInstance()).isEqualTo("lastName");
        }
    }
}
