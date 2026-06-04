package com.jobby.userservice.domain.vo;

import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.domain.models.vo.shared.ImageUrl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ImageUrlTests {

    /**
     * URL_REGEX: ^https://[\w.-]+\.[a-zA-Z]{2,}(/[\w./%- ]+)?$
     * ALLOWED_EXTENSIONS: jpg, jpeg, png, webp
     */
    private static final String VALID_URL = "https://cdn.example.com/images/profile.jpg";

    @Nested
    class OfMethod {

        @ParameterizedTest(name = "when value is {1}")
        @DisplayName("null or blank returns failure")
        @MethodSource("com.jobby.boundaries.NullityBoundaries#getWithLabels")
        void givenNullOrBlank_whenOf_returnsValidationFailure(String value, String nullityType) {
            var result = ImageUrl.of(value);

            var expected = ValidationChain.create()
                    .validateNotBlank(value, "image-url")
                    .build();

            ResultAssertions.assertFailure(result, expected);
        }

        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("invalid URL returns failure")
        @MethodSource("com.jobby.boundaries.UrlBoundaries#invalidUrlCases")
        void givenInvalidUrl_whenOf_returnsValidationFailure(String url) {
            var result = ImageUrl.of(url);

            ResultAssertions.assertFailure(result);
        }

        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("invalid extension returns failure")
        @MethodSource("com.jobby.boundaries.UrlBoundaries#invalidExtensionCases")
        void givenInvalidExtension_whenOf_returnsValidationFailure(String url) {
            var result = ImageUrl.of(url);

            ResultAssertions.assertFailure(result);
        }

        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("valid returns success")
        @MethodSource("com.jobby.boundaries.UrlBoundaries#validCases")
        void givenValid_whenOf_returnsSuccess(String url) {
            var result = ImageUrl.of(url);

            ResultAssertions.assertSuccess(result);
            Assertions.assertEquals(url, result.data().getValue());
        }
    }


    @Nested
    class OnMethod {

        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("always sets value")
        @MethodSource("casesOfOn")
        void givenAnyValue_whenOn_returnsValue(String value) {
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
