package com.jobby.userservice.domain.vo;

import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.domain.models.vo.shared.Email;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;

public class EmailTests {

    private static final String VALID_EMAIL = "user@example.com";

    @Nested
    class OfMethod {

        @ParameterizedTest(name = "when value is {1}")
        @DisplayName("null or blank returns failure")
        @MethodSource("com.jobby.boundaries.NullityBoundaries#getWithLabels")
        void givenNullOrBlank_whenOf_returnsValidationFailure(String email,
                                                                     String nullityType) {
            var result = Email.of(email);

            var expected = ValidationChain.create()
                    .validateNotBlank(email, "recoveryEmail address")
                    .build();

            ResultAssertions.assertFailure(result, expected);
        }

        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("invalid format returns failure")
        @MethodSource("com.jobby.boundaries.EmailBoundaries#invalidFormatCases")
        void givenInvalidFormat_whenOf_returnsValidationFailure(String email) {
            var result = Email.of(email);

            ResultAssertions.assertFailure(result);
        }

        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("valid returns success")
        @MethodSource("com.jobby.boundaries.EmailBoundaries#validCases")
        void givenValid_whenOf_returnsSuccess(String email) {
            var result = Email.of(email);

            ResultAssertions.assertSuccess(result);
            Assertions.assertEquals(email, result.data().getEmail());
        }
    }

    @Nested
    class OnMethod {

        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("always sets value")
        @MethodSource("casesOfOn")
        void givenAnyValue_whenOn_returnsValue(String email) {
            var result = Email.reconstruct(email);

            Assertions.assertSame(email, result.getEmail());
        }

        private static Stream<Arguments> casesOfOn() {
            return Stream.of(
                    Arguments.of((Object) null),
                    Arguments.of(""),
                    Arguments.of(VALID_EMAIL),
                    Arguments.of("not-an-recoveryEmail-no-validation")
            );
        }
    }
}
