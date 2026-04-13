package com.jobby.userservice.domain.factory;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.domain.models.IdentificationType;
import com.jobby.userservice.domain.models.User;
import com.jobby.userservice.domain.vo.Email;
import com.jobby.userservice.domain.vo.IdentificationNumber;
import com.jobby.userservice.domain.vo.Phone;

import java.time.Instant;

public class UserFactory {

    public static Result<User, Error> create(long id, int identificationTypeId, String firstName,
                                             String lastName, String role, String identificationNumber,
                                             IdentificationType identificationType, String email, String phone){
        var user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setIdentificationNumber(new IdentificationNumber(identificationNumber));
        user.setEmail(new Email(email));
        user.setPhone(new Phone(phone));
        user.setActive(true);
        user.setCreatedAt(Instant.now());
        user.setModifiedAt(Instant.now());
        user.setIdentificationTypeId(identificationTypeId);

        return ValidationChain.create()
                .validateNotBlank(firstName, "first name")
                .validateNotBlank(lastName, "last name")
                .validateNotBlank(role, "role")
                .add(user.getEmail().validate())
                .add(user.getPhone().validate())
                .add(user.getIdentificationNumber().validate(identificationType))
                .build()
                .map(v -> user);
    }
}
