package com.jobby.userservice.domain.models;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.vo.Email;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
        var owner = new Owner();
        owner.id = id;
        owner.userId = userId;
        owner.secureParameters = secureParameters;
        owner.createdAt = Instant.now();
        owner.modifiedAt = Instant.now();
        return Result.success(owner);
    }

    public static Owner reconstruct(long id,
                                    long userId,
                                    String alternativeEmail,
                                    Map<String, String> secureParameters,
                                    Instant createdAt,
                                    Instant modifiedAt){
        return new Owner(id,
                userId,
                Email.on(alternativeEmail),
                secureParameters,
                createdAt,
                modifiedAt);
    }
}
