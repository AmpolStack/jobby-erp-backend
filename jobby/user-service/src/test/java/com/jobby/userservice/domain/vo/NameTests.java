package com.jobby.userservice.domain.vo;

import com.jobby.boundaries.NameBoundaries;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.domain.models.vo.shared.Name;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;

public class NameTests {
    private static final String VALID_NAME = "Rodrigo Torres";
    private static final String VALID_FIELD_NAME = "Field";

    @Nested
    class OfMethod {

        @ParameterizedTest(name = "when value is {1}")
        @DisplayName("null or blank returns failure")
        @MethodSource("com.jobby.boundaries.NullityBoundaries#getWithLabels")
        void givenNullOrBlank_whenOf_returnsValidationFailure(
                String name,
                String nullityType
        ) {
            var result = Name.of(name, VALID_FIELD_NAME);

            var expectedResult = ValidationChain
                    .create()
                    .validateNotBlank(name, VALID_FIELD_NAME)
                    .build();

            ResultAssertions.assertFailure(result, expectedResult);
        }

        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("too short returns failure")
        @MethodSource("com.jobby.boundaries.NameBoundaries#tooShortCases")
        void givenTooShort_whenOf_returnsValidationFailure(
                String name,
                int charsLength
        ) {
            var result = Name.of(name, VALID_FIELD_NAME);

            var expectedResult = ValidationChain.create()
                    .validateGreaterOrEqualsThan(charsLength, NameBoundaries.MIN_LENGTH, VALID_FIELD_NAME)
                    .build();

            ResultAssertions.assertFailure(result, expectedResult);
        }

        @ParameterizedTest(name = "when length is {0}")
        @DisplayName("too long returns failure")
        @MethodSource("com.jobby.boundaries.NameBoundaries#tooLongCases")
        void givenTooLong_whenOf_returnsValidationFailure(
                int charsLength
        ){
            var name = "a".repeat(charsLength);

            var result = Name.of(name, VALID_FIELD_NAME);

            var expectedResult = ValidationChain.create()
                    .validateSmallerOrEqualsThan(name.length(), NameBoundaries.MAX_LENGTH, VALID_FIELD_NAME)
                    .build();

            ResultAssertions.assertFailure(result, expectedResult);
        }

        @ParameterizedTest(name = "when length is {0}")
        @DisplayName("valid length returns success")
        @MethodSource("com.jobby.boundaries.NameBoundaries#validLengthCases")
        void givenValidLength_whenOf_returnsSuccess(
                int nameLength
        ){
            var name = "a".repeat(nameLength);

            var result = Name.of(name, VALID_FIELD_NAME);
            ResultAssertions.assertSuccess(result);
            Assertions.assertSame(name, result.data().getValue());
        }

        @RepeatedTest(50)
        @DisplayName("valid always returns success")
        void givenValid_whenOf_returnsSuccess(){
            var result = Name.of(VALID_NAME, VALID_FIELD_NAME);
            ResultAssertions.assertSuccess(result);
        }
    }

    @Nested
    class OnMethod {
        @ParameterizedTest(name = "when value is {0}")
        @DisplayName("always sets value")
        @MethodSource("casesOfOn")
        void givenAnyValue_whenOn_returnsValue(
                String name
        ){
            var result = Name.on(name);
            Assertions.assertSame(name, result.getValue());
        }

        private static Stream<Arguments> casesOfOn(){
            return Stream.of(
                    Arguments.of((Object) null),
                    Arguments.of(""),
                    Arguments.of(VALID_NAME)
            );
        }
    }
}
