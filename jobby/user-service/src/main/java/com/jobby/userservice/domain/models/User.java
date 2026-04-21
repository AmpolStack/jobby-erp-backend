package com.jobby.userservice.domain.models;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.domain.vo.*;
import lombok.*;
import java.time.Instant;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
            String firstName, String lastName, String role,
            String identificationNumber, IdentificationType identificationType,
            String email, String phone) {
        var user = new User();
        return ValidationChain.create()
                .validateNotBlank(role, "role")
                .build()
                .flatMap(v -> Name.of(firstName, "first name"))
                .peek(obj -> user.firstName = obj)
                .flatMap(v -> Name.of(lastName, "last name"))
                .peek(obj -> user.lastName = obj)
                .flatMap(v -> IdentificationNumber.of(identificationNumber, identificationType))
                .peek(obj -> user.identificationNumber = obj)
                .flatMap(v -> Email.of(email))
                .peek(obj -> user.email = obj)
                .flatMap(v -> Phone.of(phone))
                .map(obj -> {
                    user.phone = obj;
                    user.id = id;
                    user.identificationTypeId = identificationTypeId;
                    user.role = role;
                    user.isActive = true;
                    user.createdAt = Instant.now();
                    user.modifiedAt = Instant.now();
                    return user;
                });
    }

    public static User reconstruct(long id,
                                   Set<Contact> contacts,
                                   int identificationTypeId,
                                   String firstName,
                                   String lastName,
                                   String role,
                                   boolean isActive,
                                   String profileImageUrl,
                                   String identificationNumber,
                                   String email,
                                   String phone,
                                   Instant createdAt,
                                   Instant modifiedAt){
        return new User(id,
                contacts,
                identificationTypeId,
                Name.on(firstName),
                Name.on(lastName),
                role,
                isActive,
                ImageUrl.on(profileImageUrl),
                IdentificationNumber.on(identificationNumber),
                Email.on(email),
                Phone.on(phone),
                createdAt,
                modifiedAt);
    }
}
