package com.jobby.userservice.domain.models;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.domain.enums.Role;
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
    private Role role;
    private boolean isActive;
    private ImageUrl profileImageUrl;
    private IdentificationNumber identificationNumber;
    private Email email;
    private Phone phone;
    private Instant createdAt;
    private Instant modifiedAt;

    public static Result<User, Error> create(long id,
            int identificationTypeId, Name firstName, Name lastName, Role role,
            IdentificationNumber identificationNumber, Email email, Phone phone) {

        return ValidationChain.create()
                .validateNotNull(role, "role")
                .validateNotNull(firstName, "first name")
                .validateNotNull(lastName, "last name")
                .validateNotNull(identificationNumber, "identification number")
                .validateNotNull(email, "email address")
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
                                   Role role,
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


    public Result<Void, Error> updateImageUrl(ImageUrl imageUrl){
        return ValidationChain.create()
                .validateNotNull(imageUrl, "user profile image url")
                .build()
                .peek(v -> {
                    this.profileImageUrl = imageUrl;
                    this.modifiedAt = Instant.now();
                });
    }

    public Result<Void, Error> removeProfileImage(){
        return ValidationChain.create()
                .validateIf(this.getProfileImageUrl() == null
                                || this.getProfileImageUrl().getValue() == null,
                        () -> Result.failure(ErrorType.VALIDATION_ERROR,
                        new Field("Profile image",
                                "The user does not have a profile picture")) )
                .build()
                .peek(v -> {
                    this.profileImageUrl = null;
                    this.modifiedAt = Instant.now();
                });
    }


}
