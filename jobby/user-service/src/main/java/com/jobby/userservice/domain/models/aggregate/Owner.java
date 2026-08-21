package com.jobby.userservice.domain.models.aggregate;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.domain.models.vo.shared.Email;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
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

    public Result<Void, Error> updateRecoveryEmail(Email alternativeEmail) {
        return ValidationChain.create()
                .validateNotNull(alternativeEmail, "owner recovery AlternativeEmail")
                .build()
                .peek(v -> {
                    this.alternativeEmail = alternativeEmail;
                    this.modifiedAt = Instant.now();
                });
    }

    public Result<Void, Error> removeRecoveryEmail() {
        return ValidationChain.create().build()
                .map(v -> {
                    this.alternativeEmail = null;
                    this.modifiedAt = Instant.now();
                    return null;
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
