package com.jobby.userservice.domain.models;

import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.userservice.ResultAssertions;
import com.jobby.userservice.TestingValidationChainCaster;
import com.jobby.userservice.TestingValidationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.Set;
import java.util.stream.Stream;

public class UserTest {

    private static final long VALID_ID = 1;
    private static final int VALID_IDENTIFICATION_TYPE_ID = 1;
    private static final String VALID_FIRSTNAME = "Fernanda";
    private static final String VALID_LASTNAME = "Sepúlveda Tapia";
    private static final String VALID_ROLE = "owner";
    private static final String VALID_IDENTIFICATION_NUMBER = "1097095371";
    private static final String VALID_EMAIL = "test@test.com";
    private static final String VALID_PHONE = "3134570509";
    private static final IdentificationType VALID_IDENTIFICATION_TYPE =
            IdentificationType.reconstruct(
                    VALID_IDENTIFICATION_TYPE_ID,
                    93,
                    "Passport",
                    8,
                    10,
                    "^\\d+$",
                    "PSP",
                    Set.of("numbers"));

    @ParameterizedTest(name = "When {0} is null, should returns failure")
    @DisplayName("Given required fields are null, when creating a user, then it returns a validation error.")
    @MethodSource("casesOfNullity")
    void create_WhenRequiredFieldIsNull_ShouldReturnValidationError(
            String fieldName,
            TestingValidationType testingValidationType,
            String firstName,
            String lastName,
            String role,
            String identificationNumber,
            String email,
            String phone,
            IdentificationType identificationType
    ){
        // Act
        var result = User.create(VALID_ID,
                VALID_IDENTIFICATION_TYPE_ID,
                firstName,
                lastName,
                role,
                identificationNumber,
                identificationType,
                email,
                phone);

        // Assert
        var expectedResult = TestingValidationChainCaster.buildExpectedError(testingValidationType, fieldName);

        ResultAssertions.assertFailure(result, expectedResult);
    }


    @Test
    @DisplayName("Given all required fields are null, when creating a user, then it returns a validation error.")
    void create_WhenAllRequiredFieldsAreNull_ShouldReturnsValidationError(){
        // Act
        var result = User.create(VALID_ID,
                VALID_IDENTIFICATION_TYPE_ID,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        // Assert
        ResultAssertions.assertFailure(result, ErrorType.VALIDATION_ERROR);
    }

    @RepeatedTest(10)
    @DisplayName("Given all field are correct, when creating a user, then it returns success")
    void create_WhenAllFieldsAreCorrect_ShouldReturnsSuccess(){
        // Act
        var result = User.create(VALID_ID,
                VALID_IDENTIFICATION_TYPE_ID,
                VALID_FIRSTNAME,
                VALID_LASTNAME,
                VALID_ROLE,
                VALID_IDENTIFICATION_NUMBER,
                VALID_IDENTIFICATION_TYPE,
                VALID_EMAIL,
                VALID_PHONE);

        // Assert
        ResultAssertions.assertSuccess(result);
    }

    private static Stream<Arguments> casesOfNullity() {
        return Stream.of(
                Arguments.of("first name", TestingValidationType.NOT_BLANK,
                        null, VALID_LASTNAME, VALID_ROLE, VALID_IDENTIFICATION_NUMBER,
                        VALID_EMAIL, VALID_PHONE, VALID_IDENTIFICATION_TYPE),
                Arguments.of("last name", TestingValidationType.NOT_BLANK,
                        VALID_FIRSTNAME, null, VALID_ROLE, VALID_IDENTIFICATION_NUMBER,
                        VALID_EMAIL, VALID_PHONE, VALID_IDENTIFICATION_TYPE),
                Arguments.of("role", TestingValidationType.NOT_BLANK,
                        VALID_FIRSTNAME, VALID_LASTNAME, null, VALID_IDENTIFICATION_NUMBER,
                        VALID_EMAIL, VALID_PHONE, VALID_IDENTIFICATION_TYPE),
                Arguments.of("identification number", TestingValidationType.NOT_BLANK,
                        VALID_FIRSTNAME, VALID_LASTNAME, VALID_ROLE, null,
                        VALID_EMAIL, VALID_PHONE, VALID_IDENTIFICATION_TYPE),
                Arguments.of("identification type", TestingValidationType.INTERNAL_NOT_NULL,
                        VALID_FIRSTNAME, VALID_LASTNAME, VALID_ROLE, VALID_IDENTIFICATION_NUMBER,
                        VALID_EMAIL, VALID_PHONE, null),
                Arguments.of("email address", TestingValidationType.NOT_NULL,
                        VALID_FIRSTNAME, VALID_LASTNAME, VALID_ROLE, VALID_IDENTIFICATION_NUMBER,
                        null, VALID_PHONE, VALID_IDENTIFICATION_TYPE),
                Arguments.of("phone number", TestingValidationType.NOT_NULL,
                        VALID_FIRSTNAME, VALID_LASTNAME, VALID_ROLE, VALID_IDENTIFICATION_NUMBER,
                        VALID_EMAIL, null, VALID_IDENTIFICATION_TYPE)
        );
    }
}
