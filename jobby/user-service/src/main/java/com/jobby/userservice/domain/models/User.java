package com.jobby.userservice.domain.models;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.domain.vo.*;
import lombok.*;
import java.time.Instant;
import java.util.Set;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
    private Long id;
    private Set<Contact> contacts;
    private int identificationTypeId;
    private Name firstName;
    private Name lastName;
    private String role;
    private boolean isActive;
    private ImageUrl profileImageUrl;
    private IdentificationNumber identificationNumber;
    private Email email;
    private Phone phone;
    private Instant createdAt;
    private Instant modifiedAt;

    public static Result<User, Error> create(long id, int identificationTypeId,
            Name firstName, Name lastName, String role,
            IdentificationNumber identificationNumber, Email email, Phone phone) {

        return ValidationChain.create()
                .validateNotBlank(role, "role")
                .validateNotNull(firstName, "first name")
                .validateNotNull(lastName, "last name")
                .validateNotNull(identificationNumber, "identification number")
                .validateNotNull(phone, "phone number")
                .build()
                .map(v -> new User(id,
                        null,
                        identificationTypeId,
                        firstName,
                        lastName,
                        role,
                        true,
                        null,
                        identificationNumber,
                        email,
                        phone,
                        Instant.now(),
                        Instant.now()));
    }

    public static User reconstruct(long id,
                                   Set<Contact> contacts,
                                   int identificationTypeId,
                                   Name firstName,
                                   Name lastName,
                                   String role,
                                   boolean isActive,
                                   ImageUrl profileImageUrl,
                                   IdentificationNumber identificationNumber,
                                   Email email,
                                   Phone phone,
                                   Instant createdAt,
                                   Instant modifiedAt){
        return new User(id,
                contacts,
                identificationTypeId,
                firstName,
                lastName,
                role,
                isActive,
                profileImageUrl,
                identificationNumber,
                email,
                phone,
                createdAt,
                modifiedAt);
    }
}
