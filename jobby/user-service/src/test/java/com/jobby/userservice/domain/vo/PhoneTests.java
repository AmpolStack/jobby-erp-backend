package com.jobby.userservice.domain.vo;

import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.NullityOps;
import com.jobby.userservice.ResultAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

public class PhoneTests {

    private static final String VALID_NUMBER = "3001234567";

    @Nested
    class OfMethod {

        @ParameterizedTest(name = "When number is null")
        @DisplayName("Given number is null, when of is called, then returns validation failure")
        @MethodSource("casesOfNullity")
        void of_WhenNumberIsNull_ShouldReturnValidationFailure(String number) {
            var result = Phone.of(number);

            var expected = ValidationChain.create()
                    .validateNotBlank(null, "phone number")
                    .build();

            ResultAssertions.assertFailure(result, expected);
        }

        @ParameterizedTest(name = "When number is blank -> [{0}]")
        @DisplayName("Given number is blank, when of is called, then returns validation failure")
        @MethodSource("casesOfBlank")
        void of_WhenNumberIsBlank_ShouldReturnValidationFailure(String number) {
            var result = Phone.of(number);

            var expected = ValidationChain.create()
                    .validateNotBlank("", "phone number")
                    .build();

            ResultAssertions.assertFailure(result, expected);
        }

        @ParameterizedTest(name = "When number is -> [{0}]")
        @DisplayName("Given number does not match the regex ^3\\d{9}$, when of is called, then returns validation failure")
        @MethodSource("casesOfInvalidFormat")
        void of_WhenNumberHasInvalidFormat_ShouldReturnValidationFailure(String number) {
            var result = Phone.of(number);

            ResultAssertions.assertFailure(result);
        }

        @ParameterizedTest(name = "When number is -> {0}")
        @DisplayName("Given number is valid, when of is called, then returns success and stores number")
        @ValueSource(strings = {"3001234567", "3101234567", "3201234567", "3991234567"})
        void of_WhenNumberIsValid_ShouldReturnSuccess(String number) {
            var result = Phone.of(number);

            ResultAssertions.assertSuccess(result);
            Assertions.assertEquals(number, result.data().getNumber());
        }

        private static Stream<Arguments> casesOfNullity() {
            return Stream.of(Arguments.of((Object) null));
        }

        private static Stream<Arguments> casesOfBlank() {
            return NullityOps.BLANK_VALUES.stream().map(Arguments::of);
        }

        private static Stream<Arguments> casesOfInvalidFormat() {
            return Stream.of(
                    Arguments.of("1001234567"),   // does not start with 3
                    Arguments.of("300123456"),    // 9 digits (too short)
                    Arguments.of("30012345678"),  // 11 digits (too long)
                    Arguments.of("3001234abc"),   // contains letters
                    Arguments.of("+573001234567") // includes country code
            );
        }
    }

    @Nested
    class OnMethod {

        @ParameterizedTest(name = "When number -> [{0}]")
        @DisplayName("on() always sets the raw number without validation")
        @MethodSource("casesOfOn")
        void on_AlwaysSetsNumber(String number) {
            var result = Phone.on(number);

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

    @Nested
    class FormattingMethods {

        @ParameterizedTest(name = "When number -> {0}, expected E.164 -> {1}")
        @DisplayName("getNumberAsE164() returns '+57 <number>' format")
        @MethodSource("casesOfE164")
        void getNumberAsE164_ShouldReturnE164Format(String number, String expected) {
            var phone = Phone.on(number);

            Assertions.assertEquals(expected, phone.getNumberAsE164());
        }

        @ParameterizedTest(name = "When number -> {0}, expected display -> {1}")
        @DisplayName("getNumberAsDisplay() returns '(+57) <number>' format")
        @MethodSource("casesOfDisplay")
        void getNumberAsDisplay_ShouldReturnDisplayFormat(String number, String expected) {
            var phone = Phone.on(number);

            Assertions.assertEquals(expected, phone.getNumberAsDisplay());
        }

        private static Stream<Arguments> casesOfE164() {
            return Stream.of(
                    Arguments.of("3001234567", "+57 3001234567"),
                    Arguments.of("3101234567", "+57 3101234567")
            );
        }

        private static Stream<Arguments> casesOfDisplay() {
            return Stream.of(
                    Arguments.of("3001234567", "(+57) 3001234567"),
                    Arguments.of("3101234567", "(+57) 3101234567")
            );
        }
    }
}
