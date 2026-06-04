package com.jobby.userservice.domain.vo;

import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.domain.models.reference.IdentificationType;
import com.jobby.userservice.domain.models.vo.shared.IdentificationNumber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
        @ParameterizedTest(name = "when value is {1}")
        @DisplayName("null or blank returns failure")
        @MethodSource("com.jobby.boundaries.NullityBoundaries#getWithLabels")
        void givenNullOrBlank_whenOf_returnsValidationFailure(
                String number, String nullityType){
            var result = IdentificationNumber.of(number, VALID_IDENTIFICATION_TYPE);

            var expected = ValidationChain.create()
                    .validateNotBlank(number, "identification number")
                    .build();

            ResultAssertions.assertFailure(result, expected);
        }

        @Test
        @DisplayName("null type returns failure")
        void givenNullType_whenOf_returnsValidationFailure(){
            var result = IdentificationNumber.of(VALID_NUMBER, null);

            var expected = ValidationChain.create()
                            .validateInternalNotNull(null, "identification type")
                    .build();

            ResultAssertions.assertFailure(result, expected);
        }

        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("too short returns failure")
        @MethodSource("casesOfToShort")
        void givenTooShort_whenOf_returnsValidationFailure(
                String number,
                int charsLength
        ){
            var result = IdentificationNumber.of(number, VALID_IDENTIFICATION_TYPE);

            var expected = ValidationChain.create()
                    .validateGreaterOrEqualsThan(-1, IDENTIFICATION_TYPE_MIN_LENGTH, "identification number (PSP)")
                    .build();

            ResultAssertions.assertFailure(result, expected);
        }

        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("too long returns failure")
        @MethodSource("casesOfToBig")
        void givenTooLong_whenOf_returnsValidationFailure(
                String number,
                int charsLength
        ){
            var result = IdentificationNumber.of(number, VALID_IDENTIFICATION_TYPE);

            var expected = ValidationChain.create()
                    .validateSmallerOrEqualsThan(Integer.MAX_VALUE, IDENTIFICATION_TYPE_MAX_LENGTH, "identification number (PSP)")
                    .build();

            ResultAssertions.assertFailure(result, expected);
        }

        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("invalid format returns failure")
        @MethodSource("com.jobby.boundaries.IdentificationNumberBoundaries#invalidFormatCases")
        void givenInvalidFormat_whenOf_returnsValidationFailure(String number) {
            var result = IdentificationNumber.of(number, VALID_IDENTIFICATION_TYPE);

            ResultAssertions.assertFailure(result);
        }

        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("valid returns success")
        @MethodSource("com.jobby.boundaries.IdentificationNumberBoundaries#validCases")
        void givenValid_whenOf_returnsSuccess(String number) {
            var result = IdentificationNumber.of(number, VALID_IDENTIFICATION_TYPE);

            ResultAssertions.assertSuccess(result);
            Assertions.assertEquals(number, result.data().getNumber());
        }

        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("trimmable returns success")
        @MethodSource("com.jobby.boundaries.IdentificationNumberBoundaries#trimmableCases")
        void givenTrimmable_whenOf_returnsSuccess(String number) {
            var result = IdentificationNumber.of(number, VALID_IDENTIFICATION_TYPE);

            ResultAssertions.assertSuccess(result);
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
    }

    @Nested
    class OnMethod {
        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("always sets value")
        @MethodSource("casesOfOn")
        void givenAnyValue_whenOn_returnsValue(String number) {
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
