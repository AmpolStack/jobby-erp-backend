package com.jobby.userservice.domain.models;

import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.NullityOps;
import com.jobby.userservice.ResultAssertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.stream.Stream;

public class UserTests {

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

    @Nested
    class CreateMethod{
        @ParameterizedTest(name = "When {0} is {2}, should returns failure")
        @DisplayName("Given required fields are null or null, when creating a user, then it returns a validation error.")
        @MethodSource("casesOfNullity")
        void create_WhenRequiredFieldIsNullOrBlank_ShouldReturnValidationError(
                String fieldName,
                String blank,
                String blankType,
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
            var expected = ValidationChain
                    .create()
                    .validateNotBlank(blank, fieldName)
                    .build();

            ResultAssertions.assertFailure(result, expected);
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

        @RepeatedTest(10)
        @DisplayName("Given all field are correct, when creating a user, then it returns success")
        void create_WhenAllIsCorrect_AlwaysSetsFields(){
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
            Assertions.assertTrue(result.data().isActive());
            Assertions.assertNull(result.data().getContacts());
            Assertions.assertNull(result.data().getProfileImageUrl());
            Assertions.assertSame(VALID_ID, result.data().getId());
            Assertions.assertSame(VALID_IDENTIFICATION_TYPE_ID, result.data().getIdentificationTypeId());
            Assertions.assertSame(VALID_FIRSTNAME, result.data().getFirstName().getValue());
            Assertions.assertSame(VALID_LASTNAME, result.data().getLastName().getValue());
            Assertions.assertSame(VALID_ROLE, result.data().getRole());
            Assertions.assertSame(VALID_IDENTIFICATION_NUMBER, result.data().getIdentificationNumber().getNumber());
            Assertions.assertSame(VALID_IDENTIFICATION_TYPE.getId(), result.data().getIdentificationTypeId());
            Assertions.assertSame(VALID_EMAIL, result.data().getEmail().getEmail());
            Assertions.assertSame(VALID_PHONE, result.data().getPhone().getNumber());
        }

        // Only instances without a dedicated Value Object are validated, since each one has its own unit tests.
        private static Stream<Arguments> casesOfNullity() {
            return NullityOps.BLANK_VALUES.stream()
                    .flatMap(blank -> Stream.of(
                            Arguments.of("role", blank, NullityOps.getNullityName(blank),
                                    VALID_FIRSTNAME, VALID_LASTNAME, blank, VALID_IDENTIFICATION_NUMBER,
                                    VALID_EMAIL, VALID_PHONE, VALID_IDENTIFICATION_TYPE))
                    );
        }
    }

    @Nested
    class ReconstructMethod{
        @ParameterizedTest
        @DisplayName("Given all field are correct, when reconstruct a user, then always sets fields")
        @MethodSource("casesOfReconstruct")
        void reconstruct_WhenAllIsCorrect_AlwaysSetsFields(
                long id,
                Set<Contact> contacts,
                int identificationTypeId,
                String firstName,
                String lastName,
                String role,
                boolean active,
                String profileImageUrl,
                String identificationNumber,
                String email,
                String phone,
                Instant createdAt,
                Instant modifiedAt
        ){
            // Act
            var result = User.reconstruct(id,
                    contacts,
                    identificationTypeId,
                    firstName,
                    lastName,
                    role,
                    active,
                    profileImageUrl,
                    identificationNumber,
                    email,
                    phone,
                    createdAt,
                    modifiedAt);

            // Assert
            Assertions.assertNotNull(result);

            if(contacts == null){
                Assertions.assertNull(result.getContacts());
            }
            else{
                Assertions.assertSame(contacts.size(), result.getContacts().size());
                Assertions.assertEquals(contacts, result.getContacts());
            }

            Assertions.assertSame(id, result.getId());
            Assertions.assertSame(identificationTypeId, result.getIdentificationTypeId());
            Assertions.assertSame(firstName, result.getFirstName().getValue());
            Assertions.assertSame(lastName, result.getLastName().getValue());
            Assertions.assertSame(role, result.getRole());
            Assertions.assertSame(identificationNumber, result.getIdentificationNumber().getNumber());
            Assertions.assertSame(email, result.getEmail().getEmail());
            Assertions.assertSame(phone, result.getPhone().getNumber());
            Assertions.assertEquals(createdAt, result.getCreatedAt());
            Assertions.assertSame(modifiedAt, result.getModifiedAt());
            Assertions.assertSame(active, result.isActive());
        }

        private static Stream<Arguments> casesOfReconstruct(){
            var c1 =Contact.reconstruct(1L, 1, "Personal WhatsApp", "My personal WhatsApp", true, "+57 3134570509");
            var c2 = Contact.reconstruct(2L, 1, "Business WhatsApp", "My business WhatsApp", false, "+57 0390940242");
            var contacts = Set.of(c1, c2);

            return Stream.of(
                    Arguments.of(VALID_ID, contacts, VALID_IDENTIFICATION_TYPE_ID, VALID_FIRSTNAME, VALID_LASTNAME,
                            VALID_ROLE, true, "https://jobby.container.images.idf", VALID_IDENTIFICATION_NUMBER, VALID_EMAIL,
                            VALID_PHONE, Instant.now(), Instant.now()),
                    Arguments.of(VALID_ID, null, VALID_IDENTIFICATION_TYPE_ID, VALID_FIRSTNAME, VALID_LASTNAME,
                            VALID_ROLE, false, null, VALID_IDENTIFICATION_NUMBER, VALID_EMAIL,
                            VALID_PHONE, LocalDateTime.of(2026, 5, 20, 15, 30).toInstant(ZoneOffset.UTC), LocalDateTime.of(2025, 7, 10, 15, 30).toInstant(ZoneOffset.UTC)),
                    Arguments.of(VALID_ID, null, VALID_IDENTIFICATION_TYPE_ID, null, null,
                            null, false, null, null, null,
                            null, null, null)
            );
        }
    }

}
