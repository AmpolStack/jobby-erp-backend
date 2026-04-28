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

public class ImageUrlTests {

    /**
     * URL_REGEX: ^https://[\w.-]+\.[a-zA-Z]{2,}(/[\w./%- ]+)?$
     * ALLOWED_EXTENSIONS: jpg, jpeg, png, webp
     */
    private static final String VALID_URL = "https://cdn.example.com/images/profile.jpg";

    @Nested
    class OfMethod {

        @ParameterizedTest(name = "When value is {1}")
        @DisplayName("Given value is null, when of is called, then returns validation failure")
        @MethodSource("casesOfNullity")
        void of_WhenValueIsNull_ShouldReturnValidationFailure(String value, String nullityType) {
            var result = ImageUrl.of(value);

            var expected = ValidationChain.create()
                    .validateNotBlank(value, "image-url")
                    .build();

            ResultAssertions.assertFailure(result, expected);
        }

        @ParameterizedTest(name = "When URL is -> [{0}]")
        @DisplayName("Given URL does not match the URL regex, when of is called, then returns validation failure")
        @MethodSource("casesOfInvalidUrl")
        void of_WhenUrlFormatIsInvalid_ShouldReturnValidationFailure(String url) {
            var result = ImageUrl.of(url);

            ResultAssertions.assertFailure(result);
        }

        @ParameterizedTest(name = "When URL is -> [{0}]")
        @DisplayName("Given URL is valid but has unsupported extension, when of is called, then returns validation failure")
        @MethodSource("casesOfInvalidExtension")
        void of_WhenExtensionIsUnsupported_ShouldReturnValidationFailure(String url) {
            var result = ImageUrl.of(url);

            ResultAssertions.assertFailure(result);
        }

        @ParameterizedTest(name = "When URL is -> {0}")
        @DisplayName("Given URL is valid and has a supported extension, when of is called, then returns success")
        @ValueSource(strings = {
                "https://cdn.example.com/image.jpg",
                "https://cdn.example.com/image.jpeg",
                "https://cdn.example.com/image.png",
                "https://cdn.example.com/image.webp",
                "https://cdn.example.com/path/to/image.png"
        })
        void of_WhenUrlAndExtensionAreValid_ShouldReturnSuccess(String url) {
            var result = ImageUrl.of(url);

            ResultAssertions.assertSuccess(result);
            Assertions.assertEquals(url, result.data().getValue());
        }


        private static Stream<Arguments> casesOfNullity() {
            return NullityOps.BLANK_VALUES.stream()
                    .flatMap(blank -> Stream.of(
                            Arguments.of(blank, NullityOps.getNullityName(blank))
                    ));
        }

        private static Stream<Arguments> casesOfInvalidUrl() {
            return Stream.of(
                    Arguments.of("http://example.com/image.jpg"),      // http not https
                    Arguments.of("ftp://example.com/image.jpg"),       // wrong scheme
                    Arguments.of("not-a-url"),                          // no scheme at all
                    Arguments.of("https://"),                           // no domain
                    Arguments.of("https://no-tld/image.jpg")           // no TLD dot
            );
        }

        private static Stream<Arguments> casesOfInvalidExtension() {
            return Stream.of(
                    Arguments.of("https://cdn.example.com/image.gif"),
                    Arguments.of("https://cdn.example.com/image.bmp"),
                    Arguments.of("https://cdn.example.com/image.svg"),
                    Arguments.of("https://cdn.example.com/image.tiff"),
                    Arguments.of("https://cdn.example.com/image.pdf"),
                    Arguments.of("https://cdn.example.com/image.exe")
            );
        }
    }


    @Nested
    class OnMethod {

        @ParameterizedTest(name = "When value -> [{0}]")
        @DisplayName("on() always sets the raw value without validation")
        @MethodSource("casesOfOn")
        void on_AlwaysSetsValue(String value) {
            var result = ImageUrl.on(value);

            Assertions.assertSame(value, result.getValue());
        }

        private static Stream<Arguments> casesOfOn() {
            return Stream.of(
                    Arguments.of((Object) null),
                    Arguments.of(""),
                    Arguments.of(VALID_URL),
                    Arguments.of("http://invalid-but-no-validation.gif")
            );
        }
    }
}
