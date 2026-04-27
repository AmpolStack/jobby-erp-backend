package com.jobby.userservice.domain.vo;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.NullityOps;
import com.jobby.userservice.ResultAssertions;
import com.jobby.userservice.domain.models.IdentificationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Set;
import java.util.stream.Stream;

public class IdentificationNumberTests {
    private static final String VALID_NUMBER = "1097095371";
    private static final int IDENTIFICATION_TYPE_MIN_LENGTH = 8;
    private static final int IDENTIFICATION_TYPE_MAX_LENGTH = 10;
    private static final IdentificationType VALID_IDENTIFICATION_TYPE
            = IdentificationType.reconstruct(1,
                    93,
                    "Passport",
                    IDENTIFICATION_TYPE_MIN_LENGTH,
                    IDENTIFICATION_TYPE_MAX_LENGTH,
                    "^\\d+$",
                    "PSP",
                    Set.of("numbers"));

    @Nested
    class OfMethod{
        @ParameterizedTest(name = "When {2} is null")
        @DisplayName("Given required fields are null, when method of is called, than returns validation failure")
        @MethodSource("casesOfNullity")
        void of_WhenRequiredFieldsAreNull_ShouldReturnValidationFailure(
                String number,
                IdentificationType type,
                String fieldName,
                Result<Void, Error> expected
        ){
            // Act
            var result = IdentificationNumber.of(number, type);

            // Assert
            ResultAssertions.assertFailure(result, expected);
        }

        @ParameterizedTest(name = "When {2} is blank")
        @DisplayName("Given required fields are blank, when method of is called, than returns validation failure")
        @MethodSource("casesOfBlank")
        void of_WhenRequiredFieldsAreBlank_ShouldReturnValidationFailure(
                String number,
                IdentificationType type,
                String fieldName
        ){
            // Act
            var result = IdentificationNumber.of(number, type);

            // Assert
            var expected = ValidationChain.create()
                    .validateNotBlank("", fieldName)
                    .build();

            ResultAssertions.assertFailure(result, expected);
        }

        @ParameterizedTest(name = "When number length is equals to: {1} -> [{0}]")
        @DisplayName("Given number field is to short, when of method is called, then returns validation error")
        @MethodSource("casesOfToShort")
        void of_WhenNumberIsToShort_ShouldReturnValidationFailure(
                String number,
                int charsLength
        ){
            // Act
            var result = IdentificationNumber.of(number, VALID_IDENTIFICATION_TYPE);

            // Assert
            var expected = ValidationChain.create()
                    .validateGreaterOrEqualsThan(-1, IDENTIFICATION_TYPE_MIN_LENGTH, "identification number (PSP)")
                    .build();

            ResultAssertions.assertFailure(result, expected);
        }

        @ParameterizedTest(name = "When number length is equals to: {1} -> [{0}]")
        @DisplayName("Given number field is to big, when of method is called, then returns validation error")
        @MethodSource("casesOfToBig")
        void of_WhenNumberIsToBig_ShouldReturnValidationFailure(
                String number,
                int charsLength
        ){
            // Act
            var result = IdentificationNumber.of(number, VALID_IDENTIFICATION_TYPE);

            // Assert
            var expected = ValidationChain.create()
                    .validateSmallerOrEqualsThan(Integer.MAX_VALUE, IDENTIFICATION_TYPE_MAX_LENGTH, "identification number (PSP)")
                    .build();

            ResultAssertions.assertFailure(result, expected);
        }

        private static Stream<Arguments> casesOfNullity(){
            return Stream.of(
                    Arguments.of(null, VALID_IDENTIFICATION_TYPE, "identification number",
                            ValidationChain.create().
                            validateNotNull(null, "identification number")
                            .build()),
                    Arguments.of(VALID_NUMBER, null, "identification type",
                            ValidationChain.create()
                            .validateInternalNotNull(null, "identification type")
                            .build()
                    )
            );
        }

        private static Stream<Arguments> casesOfBlank(){
            return NullityOps.BLANK_VALUES.stream()
                    .flatMap(blank ->
                            Stream.of(
                                    Arguments.of(blank,
                                            VALID_IDENTIFICATION_TYPE,
                                            "identification number")
                            )
                            );
        }

        private static Stream<Arguments> casesOfToShort(){
            return Stream.of(
                    Arguments.of("a", 1),
                    Arguments.of("a".repeat(2), 2),
                    Arguments.of("a".repeat(3), 3),
                    Arguments.of("a".repeat(4), 4),
                    Arguments.of("a".repeat(5), 5),
                    Arguments.of("a".repeat(6), 6),
                    Arguments.of("a".repeat(7), 7),
                    Arguments.of("    a     ", 1),
                    Arguments.of("    a", 1),
                    Arguments.of("a    ", 1)
            );
        }

        private static Stream<Arguments> casesOfToBig(){
            return Stream.of(
                    Arguments.of("a".repeat(11), 11),
                    Arguments.of("a".repeat(12), 12),
                    Arguments.of("a".repeat(20), 20),
                    Arguments.of("a".repeat(50), 50),
                    Arguments.of("a".repeat(100), 100),
                    Arguments.of("a".repeat(500), 500),
                    Arguments.of("a".repeat(2000), 2000)
            );
        }

        @ParameterizedTest(name = "When number is -> [{0}]")
        @DisplayName("Given number does not match the type regex, when of is called, then returns validation failure")
        @MethodSource("casesOfInvalidFormat")
        void of_WhenNumberDoesNotMatchRegex_ShouldReturnValidationFailure(String number) {
            // Act  — VALID_IDENTIFICATION_TYPE uses ^\\d+$ so letters fail
            var result = IdentificationNumber.of(number, VALID_IDENTIFICATION_TYPE);

            // Assert
            ResultAssertions.assertFailure(result);
        }

        @ParameterizedTest(name = "When number length is -> {0}")
        @DisplayName("Given number is valid, when of is called, then returns success and stores number")
        @ValueSource(strings = {"12345678", "123456789", "1234567890"})
        void of_WhenAllFieldsAreValid_ShouldReturnSuccess(String number) {
            // Act
            var result = IdentificationNumber.of(number, VALID_IDENTIFICATION_TYPE);

            // Assert
            ResultAssertions.assertSuccess(result);
            Assertions.assertEquals(number, result.data().getNumber());
        }

        @ParameterizedTest(name = "When number has leading/trailing spaces -> [{0}]")
        @DisplayName("Given number with surrounding spaces but valid core, when of is called, then returns success")
        @ValueSource(strings = {"  12345678  ", "  1234567890", "12345678  "})
        void of_WhenNumberHasSurroundingSpaces_ShouldReturnSuccess(String number) {
            var result = IdentificationNumber.of(number, VALID_IDENTIFICATION_TYPE);

            ResultAssertions.assertSuccess(result);
        }

        private static Stream<Arguments> casesOfInvalidFormat(){
            return Stream.of(
                    Arguments.of("abcdefgh"),
                    Arguments.of("1234567a"),
                    Arguments.of("a234567890")
            );
        }
    }

    @Nested
    class OnMethod {
        @ParameterizedTest(name = "When number -> [{0}]")
        @DisplayName("on() always sets the raw number without validation")
        @MethodSource("casesOfOn")
        void on_AlwaysSetsNumber(String number) {
            var result = IdentificationNumber.on(number);

            Assertions.assertSame(number, result.getNumber());
        }

        private static Stream<Arguments> casesOfOn() {
            return Stream.of(
                    Arguments.of((Object) null),
                    Arguments.of(""),
                    Arguments.of(VALID_NUMBER),
                    Arguments.of("invalid-no-validation")
            );
        }
    }
}
