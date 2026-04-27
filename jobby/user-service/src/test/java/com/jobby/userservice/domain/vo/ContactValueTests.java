package com.jobby.userservice.domain.vo;

import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.NullityOps;
import com.jobby.userservice.ResultAssertions;
import com.jobby.userservice.domain.models.ContactType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Map;
import java.util.stream.Stream;

public class ContactValueTests {

    private static final ContactType GENERIC_TYPE = ContactType.reconstruct(
            1, "generic", "Generic contact", ".*",
            Map.of("hint", "any value"));

    private static final ContactType PHONE_TYPE = ContactType.reconstruct(
            2, "phone", "Colombian mobile phone", "^3\\d{9}$",
            Map.of("hint", "3XXXXXXXXX"));

    private static final ContactType URL_TYPE = ContactType.reconstruct(
            3, "url", "Website URL", "^https://[\\w.-]+\\.[a-zA-Z]{2,}(/[\\w./%\\-]+)?$",
            Map.of("hint", "https://example.com"));

    private static final String VALID_PHONE_VALUE = "3001234567";
    private static final String VALID_URL_VALUE   = "https://example.com";

    @Nested
    class OfMethod {

        @ParameterizedTest(name = "When value is null")
        @DisplayName("Given value is null, when of is called, then returns validation failure")
        @MethodSource("casesOfNullity")
        void of_WhenValueIsNull_ShouldReturnValidationFailure(String value) {
            var result = ContactValue.of(value, GENERIC_TYPE);

            var expected = ValidationChain.create()
                    .validateNotBlank(null, "contact value")
                    .build();

            ResultAssertions.assertFailure(result, expected);
        }

        @ParameterizedTest(name = "When value is blank -> [{0}]")
        @DisplayName("Given value is blank, when of is called, then returns validation failure")
        @MethodSource("casesOfBlank")
        void of_WhenValueIsBlank_ShouldReturnValidationFailure(String value) {
            var result = ContactValue.of(value, GENERIC_TYPE);

            var expected = ValidationChain.create()
                    .validateNotBlank("", "contact value")
                    .build();

            ResultAssertions.assertFailure(result, expected);
        }

        @ParameterizedTest(name = "When value length is -> {0}")
        @DisplayName("Given value exceeds max length (250), when of is called, then returns validation failure")
        @ValueSource(ints = {251, 300, 500, 1000})
        void of_WhenValueExceedsMaxLength_ShouldReturnValidationFailure(int length) {
            var value = "a".repeat(length);
            var result = ContactValue.of(value, GENERIC_TYPE);

            var expected = ValidationChain.create()
                    .validateSmallerOrEqualsThan(length, 250, "contact")
                    .build();

            ResultAssertions.assertFailure(result, expected);
        }

        @ParameterizedTest(name = "When phone value is -> [{0}]")
        @DisplayName("Given value does not match the ContactType regex (phone), when of is called, then returns failure")
        @MethodSource("casesOfInvalidPhoneFormat")
        void of_WhenValueDoesNotMatchPhoneTypeRegex_ShouldReturnValidationFailure(String value) {
            var result = ContactValue.of(value, PHONE_TYPE);

            ResultAssertions.assertFailure(result);
        }

        @ParameterizedTest(name = "When URL value is -> [{0}]")
        @DisplayName("Given value does not match the ContactType regex (URL), when of is called, then returns failure")
        @MethodSource("casesOfInvalidUrlFormat")
        void of_WhenValueDoesNotMatchUrlTypeRegex_ShouldReturnValidationFailure(String value) {
            var result = ContactValue.of(value, URL_TYPE);

            ResultAssertions.assertFailure(result);
        }

        @ParameterizedTest(name = "When value -> {0} (type: phone)")
        @DisplayName("Given value is valid for PHONE type, when of is called, then returns success")
        @ValueSource(strings = {"3001234567", "3101234567", "3991234567"})
        void of_WhenValueMatchesPhoneType_ShouldReturnSuccess(String value) {
            var result = ContactValue.of(value, PHONE_TYPE);

            ResultAssertions.assertSuccess(result);
            Assertions.assertEquals(value, result.data().getValue());
        }

        @ParameterizedTest(name = "When value -> {0} (type: url)")
        @DisplayName("Given value is valid for URL type, when of is called, then returns success")
        @ValueSource(strings = {
                "https://example.com",
                "https://sub.domain.org/path/image.png"
        })
        void of_WhenValueMatchesUrlType_ShouldReturnSuccess(String value) {
            var result = ContactValue.of(value, URL_TYPE);

            ResultAssertions.assertSuccess(result);
            Assertions.assertEquals(value, result.data().getValue());
        }

        @ParameterizedTest(name = "When value length is -> {0} (generic type, accepts all)")
        @DisplayName("Given value is at the boundary length (250), when of is called, then returns success")
        @ValueSource(ints = {1, 50, 100, 250})
        void of_WhenValueIsWithinMaxLength_ShouldReturnSuccess(int length) {
            var value = "a".repeat(length);
            var result = ContactValue.of(value, GENERIC_TYPE);

            ResultAssertions.assertSuccess(result);
        }

        private static Stream<Arguments> casesOfNullity() {
            return Stream.of(Arguments.of((Object) null));
        }

        private static Stream<Arguments> casesOfBlank() {
            return NullityOps.BLANK_VALUES.stream().map(Arguments::of);
        }

        private static Stream<Arguments> casesOfInvalidPhoneFormat() {
            return Stream.of(
                    Arguments.of("1001234567"),   // starts with 1, not 3
                    Arguments.of("300123456"),    // 9 digits
                    Arguments.of("3001234abc"),   // contains letters
                    Arguments.of("+573001234567") // includes country code
            );
        }

        private static Stream<Arguments> casesOfInvalidUrlFormat() {
            return Stream.of(
                    Arguments.of("http://example.com"),   // http not https
                    Arguments.of("not-a-url"),
                    Arguments.of("ftp://example.com")
            );
        }
    }

    @Nested
    class OnMethod {

        @ParameterizedTest(name = "When value -> [{0}]")
        @DisplayName("on() always sets the raw value without validation")
        @MethodSource("casesOfOn")
        void on_AlwaysSetsValue(String value) {
            var result = ContactValue.on(value);

            Assertions.assertSame(value, result.getValue());
        }

        private static Stream<Arguments> casesOfOn() {
            return Stream.of(
                    Arguments.of((Object) null),
                    Arguments.of(""),
                    Arguments.of(VALID_PHONE_VALUE),
                    Arguments.of(VALID_URL_VALUE),
                    Arguments.of("invalid-but-no-validation-applied")
            );
        }
    }
}
