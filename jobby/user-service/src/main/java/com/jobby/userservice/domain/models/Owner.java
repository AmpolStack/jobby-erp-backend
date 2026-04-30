package com.jobby.userservice.domain.models;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.domain.vo.Email;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Owner {
    private Long id;
    private Long userId;
    private Email alternativeEmail;
    private Map<String, String> secureParameters;
    private Instant createdAt;
    private Instant modifiedAt;

    public static Result<Owner, Error> create(long id, long userId,
                                              Map<String, String> secureParameters) {
        var owner = new Owner(id,
                userId,
                null,
                secureParameters,
                Instant.now(),
                Instant.now());

        return Result.success(owner);
    }

    public Result<Void, Error> updateRecoveryEmail(Email recoveryEmail) {
        return ValidationChain.create()
                .validateNotNull(recoveryEmail, "owner recovery recoveryEmail")
                .build()
                .peek(v -> {
                    alternativeEmail = recoveryEmail;
                    modifiedAt = Instant.now();
                });
    }

    public static Owner reconstruct(long id,
                                    long userId,
                                    Email alternativeEmail,
                                    Map<String, String> secureParameters,
                                    Instant createdAt,
                                    Instant modifiedAt){
        return new Owner(id,
                userId,
                alternativeEmail,
                secureParameters,
                createdAt,
                modifiedAt);
    }
}
