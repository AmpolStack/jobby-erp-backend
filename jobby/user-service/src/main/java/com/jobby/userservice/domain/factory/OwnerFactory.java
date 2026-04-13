package com.jobby.userservice.domain.factory;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.domain.models.Owner;
import com.jobby.userservice.domain.models.User;
import com.jobby.userservice.domain.vo.Email;
import java.time.Instant;
import java.util.Map;

public class OwnerFactory {

    public static Result<Owner, Error> create(long id, String alternativeEmail, Map<String, String> secureParameters, User user){
        var owner = new Owner();
        owner.setId(id);
        owner.setAlternativeEmail(new Email(alternativeEmail));
        owner.setSecureParameters(secureParameters);
        owner.setUser(user);
        owner.setCreatedAt(Instant.now());
        owner.setModifiedAt(Instant.now());

        return ValidationChain.create()
                .validateIf(owner.getAlternativeEmail().getEmail() != null,
                        () -> owner.getAlternativeEmail().validate())
                .build()
                .map(v -> owner);
    }
}
