package com.jobby.userservice.domain.vo;

import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.NullityOps;
import com.jobby.userservice.ResultAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.stream.Stream;

public class NameTests {
    private static final String VALID_NAME = "Rodrigo Torres";
    private static final String VALID_FIELD_NAME = "Field";
    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 150;

    @Nested
    class OfMethod {

        @ParameterizedTest(name = "When {1} is {2}")
        @DisplayName("Given fields are blank, when of method is called, then returns validation error")
        @MethodSource("casesOfBlank")
        void of_WhenRequiredFieldsAreBlank_ShouldReturnsValidationFailure(
                String name,
                String fieldName,
                String emptyType
        ) {
            // Act
            var result = Name.of(name, VALID_FIELD_NAME);

            // Asserts
            var expectedResult = ValidationChain
                    .create()
                    .validateNotBlank("", VALID_FIELD_NAME)
                    .build();

            ResultAssertions.assertFailure(result, expectedResult);
        }

        @ParameterizedTest(name = "When {1} is {2}")
        @DisplayName("Given fields are null, when of method is called, then returns validation error")
        @MethodSource("casesOfNullity")
        void of_WhenRequiredFieldsAreNull_ShouldReturnsValidationFailure(
                String name,
                String fieldName,
                String emptyType
        ) {
            // Act
            var result = Name.of(name, VALID_FIELD_NAME);

            // Asserts
            var expectedResult = ValidationChain
                    .create()
                    .validateNotBlank(null, VALID_FIELD_NAME)
                    .build();

            ResultAssertions.assertFailure(result, expectedResult);
        }

        @ParameterizedTest(name = "When name length is equals to: {1} -> [{0}]")
        @DisplayName("Given name field are to short, when of method is called, then returns validation error")
        @MethodSource("casesOfToShort")
        void of_WhenNameFieldAreToShort_ShouldReturnsValidationFailure(
                String name,
                int charsLength
        ) {
            var result = Name.of(name, VALID_FIELD_NAME);

            // Asserts
            var expectedResult = ValidationChain.create()
                    .validateGreaterOrEqualsThan(charsLength, MIN_LENGTH, VALID_FIELD_NAME)
                    .build();

            ResultAssertions.assertFailure(result, expectedResult);
        }

        @ParameterizedTest(name = "When name length is equals to: {1}")
        @DisplayName("Given name field are to big, when of method is called, then returns validation error")
        @MethodSource("casesOfToBig")
        void of_WhenNameFieldAreToBig_ShouldReturnsValidationFailure(
                int charsLength
        ){
            //Arrange
            var name = "a".repeat(charsLength);

            // Act
            var result = Name.of(name, VALID_FIELD_NAME);

            // Asserts
            var expectedResult = ValidationChain.create()
                    .validateSmallerOrEqualsThan(name.length(), MAX_LENGTH, VALID_FIELD_NAME)
                    .build();

            ResultAssertions.assertFailure(result, expectedResult);
        }

        @ParameterizedTest(name = "When name length is -> {0}")
        @DisplayName("When all are correct, then returns success")
        @ValueSource(ints = {2, 5, 10, 20, 50, 75, 100, 115, 125, 150})
        void of_WhenNameFieldAreCorrect_ShouldReturnsSuccess(
                int nameLength
        ){
            // Arrange
            var name = "a".repeat(nameLength);

            // Act
            var result = Name.of(name, VALID_FIELD_NAME);
            // Asserts
            ResultAssertions.assertSuccess(result);
            Assertions.assertSame(name, result.data().getValue());
        }

        @RepeatedTest(50)
        @DisplayName("When all are correct, then set all fields always")
        void of_WhenNameFieldAreCorrect_AlwaysSetValues(){
            // Act
            var result = Name.of(VALID_NAME, VALID_FIELD_NAME);
            // Asserts
            ResultAssertions.assertSuccess(result);
        }

        private static Stream<Arguments> casesOfBlank(){
            return NullityOps.BLANK_VALUES.stream()
                    .flatMap(blank -> Stream.of(
                            Arguments.of(blank, VALID_FIELD_NAME, NullityOps.getNullityName(blank))
                    ));
        }

        private static Stream<Arguments> casesOfNullity(){
            return Stream.of(
                    Arguments.of(null, VALID_FIELD_NAME, NullityOps.getNullityName(null))
            );
        }

        private static Stream<Arguments> casesOfToShort(){
            return Stream.of(
                    Arguments.of("a", 1),
                    Arguments.of("    a     ", 1),
                    Arguments.of("    a", 1),
                    Arguments.of("a    ", 1)
            );
        }

        private static Stream<Arguments> casesOfToBig(){
            return Stream.of(
                    Arguments.of( 151),
                    Arguments.of( 200),
                    Arguments.of( 250),
                    Arguments.of( 500),
                    Arguments.of( 1000),
                    Arguments.of( 2000),
                    Arguments.of( 5000)
            );
        }
    }

    @Nested
    class OnMethod {
        @ParameterizedTest(name = "When name equals -> {0}")
        @DisplayName("When all are correct, then set all fields always")
        @MethodSource("casesOfOn")
        void on_AlwaysSetValuesAndReturnsSuccess(
                String name
        ){
            // Act
            var result = Name.on(name);
            // Asserts
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
