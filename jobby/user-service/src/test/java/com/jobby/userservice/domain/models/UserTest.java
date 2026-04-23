package com.jobby.userservice.domain.models;

import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.ResultAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.Set;

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



    @Test
    void CreateWhenAllIsNull(){
        // Act
        var result = User.create(0,
                0,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        // Assert
        Assertions.assertTrue(result.isFailure());
        Assertions.assertSame(ErrorType.VALIDATION_ERROR, result.error().getCode());
    }

    @Test
    void CreateWhenOnlyFirstNameIsNull(){
        // Act
        var result = User.create(VALID_ID,
                VALID_IDENTIFICATION_TYPE_ID,
                null,
                VALID_LASTNAME,
                VALID_ROLE,
                VALID_IDENTIFICATION_NUMBER,
                VALID_IDENTIFICATION_TYPE,
                VALID_EMAIL,
                VALID_PHONE);

        // Assert
        var expectedResult = ValidationChain
                .create()
                .validateNotBlank(null, "first name")
                .build();

        ResultAssertions.assertFailure(result, expectedResult);
    }

    @Test
    void CreateWhenOnlyLastNameIsNull(){
        // Act
        var result = User.create(VALID_ID,
                VALID_IDENTIFICATION_TYPE_ID,
                VALID_FIRSTNAME,
                null,
                VALID_ROLE,
                VALID_IDENTIFICATION_NUMBER,
                VALID_IDENTIFICATION_TYPE,
                VALID_EMAIL,
                VALID_PHONE);

        // Assert
        var expectedResult = ValidationChain
                .create()
                .validateNotBlank(null, "last name")
                .build();

        ResultAssertions.assertFailure(result, expectedResult);
    }

    @Test
    void CreateWhenOnlyRoleIsNull(){
        // Act
        var result = User.create(VALID_ID,
                VALID_IDENTIFICATION_TYPE_ID,
                VALID_FIRSTNAME,
                VALID_LASTNAME,
                null,
                VALID_IDENTIFICATION_NUMBER,
                VALID_IDENTIFICATION_TYPE,
                VALID_EMAIL,
                VALID_PHONE);

        // Assert
        var expectedResult = ValidationChain
                .create()
                .validateNotBlank(null, "role")
                .build();

        ResultAssertions.assertFailure(result, expectedResult);
    }

    @Test
    void CreateWhenOnlyIdentificationNumberIsNull(){
        // Act
        var result = User.create(VALID_ID,
                VALID_IDENTIFICATION_TYPE_ID,
                VALID_FIRSTNAME,
                VALID_LASTNAME,
                VALID_ROLE,
                null,
                VALID_IDENTIFICATION_TYPE,
                VALID_EMAIL,
                VALID_PHONE);

        // Assert
        var expectedResult = ValidationChain
                .create()
                .validateNotBlank(null, "identification number")
                .build();

        ResultAssertions.assertFailure(result, expectedResult);
    }

    @Test
    void CreateWhenOnlyIdentificationTypeIsNull(){
        // Act
        var result = User.create(VALID_ID,
                VALID_IDENTIFICATION_TYPE_ID,
                VALID_FIRSTNAME,
                VALID_LASTNAME,
                VALID_ROLE,
                VALID_IDENTIFICATION_NUMBER,
                null,
                VALID_EMAIL,
                VALID_PHONE);

        // Assert
        var expectedResult = ValidationChain
                .create()
                .validateInternalNotNull(null, "identification type")
                .build();

        ResultAssertions.assertFailure(result, expectedResult);
    }

    @Test
    void CreateWhenOnlyEmailIsNull(){
        // Act
        var result = User.create(VALID_ID,
                VALID_IDENTIFICATION_TYPE_ID,
                VALID_FIRSTNAME,
                VALID_LASTNAME,
                VALID_ROLE,
                VALID_IDENTIFICATION_NUMBER,
                VALID_IDENTIFICATION_TYPE,
                null,
                VALID_PHONE);

        // Assert
        var expectedResult = ValidationChain
                .create()
                .validateNotNull(null, "email address")
                .build();

        ResultAssertions.assertFailure(result, expectedResult);
    }

    @Test
    void CreateWhenOnlyPhoneIsNull(){
        // Act
        var result = User.create(VALID_ID,
                VALID_IDENTIFICATION_TYPE_ID,
                VALID_FIRSTNAME,
                VALID_LASTNAME,
                VALID_ROLE,
                VALID_IDENTIFICATION_NUMBER,
                VALID_IDENTIFICATION_TYPE,
                VALID_EMAIL,
                null);

        // Assert
        var expectedResult = ValidationChain
                .create()
                .validateNotNull(null, "phone number")
                .build();

        ResultAssertions.assertFailure(result, expectedResult);
    }
}
