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

public class EmailTests {

    private static final String VALID_EMAIL = "user@example.com";

    @Nested
    class OfMethod {

        @ParameterizedTest(name = "When email is {1}")
        @DisplayName("Given email is null or blank, when of is called, then returns validation failure")
        @MethodSource("casesOfNullity")
        void of_WhenEmailIsNullOrBlank_ShouldReturnValidationFailure(String email,
                                                                     String nullityType) {
            var result = Email.of(email);

            var expected = ValidationChain.create()
                    .validateNotBlank(email, "email address")
                    .build();

            ResultAssertions.assertFailure(result, expected);
        }

        @ParameterizedTest(name = "When email is -> [{0}]")
        @DisplayName("Given email has invalid format, when of is called, then returns validation failure")
        @MethodSource("casesOfInvalidFormat")
        void of_WhenEmailHasInvalidFormat_ShouldReturnValidationFailure(String email) {
            var result = Email.of(email);

            ResultAssertions.assertFailure(result);
        }

        @ParameterizedTest(name = "When email is -> [{0}]")
        @DisplayName("Given email is valid, when of is called, then returns success and stores email")
        @ValueSource(strings = {
                "user@example.com",
                "user.name+tag@sub.domain.org",
                "USER@EXAMPLE.COM",
                "u@e.co"
        })
        void of_WhenEmailIsValid_ShouldReturnSuccess(String email) {
            var result = Email.of(email);

            ResultAssertions.assertSuccess(result);
            Assertions.assertEquals(email, result.data().getEmail());
        }

        private static Stream<Arguments> casesOfNullity() {
            return NullityOps.BLANK_VALUES.stream()
                    .flatMap(blank -> Stream.of(
                            Arguments.of(blank, NullityOps.getNullityName(blank))
                    ));
        }

        private static Stream<Arguments> casesOfInvalidFormat() {
            return Stream.of(
                    Arguments.of("not-an-email"),
                    Arguments.of("missing@tld"),          // no dot after TLD
                    Arguments.of("@nodomain.com"),
                    Arguments.of("no-at-sign.com"),
                    Arguments.of("spaces in@email.com"),
                    Arguments.of("double@@at.com")
            );
        }
    }

    @Nested
    class OnMethod {

        @ParameterizedTest(name = "When email -> [{0}]")
        @DisplayName("on() always sets the raw email without validation")
        @MethodSource("casesOfOn")
        void on_AlwaysSetsEmail(String email) {
            var result = Email.on(email);

            Assertions.assertSame(email, result.getEmail());
        }

        private static Stream<Arguments> casesOfOn() {
            return Stream.of(
                    Arguments.of((Object) null),
                    Arguments.of(""),
                    Arguments.of(VALID_EMAIL),
                    Arguments.of("not-an-email-no-validation")
            );
        }
    }
}
