package com.jobby.userservice.domain.vo;

import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.domain.models.vo.shared.Phone;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;

public class PhoneTests {

    private static final String VALID_NUMBER = "3001234567";

    @Nested
    class OfMethod {

        @ParameterizedTest(name = "when value is {1}")
        @DisplayName("null or blank returns failure")
        @MethodSource("com.jobby.boundaries.NullityBoundaries#getWithLabels")
        void givenNullOrBlank_whenOf_returnsValidationFailure(String number, String nullityType) {
            var result = Phone.of(number);

            var expected = ValidationChain.create()
                    .validateNotBlank(number, "phone number")
                    .build();

            ResultAssertions.assertFailure(result, expected);
        }

        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("invalid format returns failure")
        @MethodSource("com.jobby.boundaries.PhoneBoundaries#invalidFormatCases")
        void givenInvalidFormat_whenOf_returnsValidationFailure(String number) {
            var result = Phone.of(number);

            ResultAssertions.assertFailure(result);
        }

        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("valid returns success")
        @MethodSource("com.jobby.boundaries.PhoneBoundaries#validCases")
        void givenValid_whenOf_returnsSuccess(String number) {
            var result = Phone.of(number);

            ResultAssertions.assertSuccess(result);
            Assertions.assertEquals(number, result.data().getNumber());
        }
    }

    @Nested
    class OnMethod {

        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("always sets value")
        @MethodSource("casesOfOn")
        void givenAnyValue_whenOn_returnsValue(String number) {
            var result = Phone.reconstruct(number);

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

        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("returns E.164 format")
        @MethodSource("com.jobby.boundaries.PhoneBoundaries#e164Cases")
        void givenNumber_whenGetNumberAsE164_returnsE164Format(String number, String expected) {
            var phone = Phone.reconstruct(number);

            Assertions.assertEquals(expected, phone.getNumberAsE164());
        }

        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("returns display format")
        @MethodSource("com.jobby.boundaries.PhoneBoundaries#displayCases")
        void givenNumber_whenGetNumberAsDisplay_returnsDisplayFormat(String number, String expected) {
            var phone = Phone.reconstruct(number);

            Assertions.assertEquals(expected, phone.getNumberAsDisplay());
        }
    }
}
