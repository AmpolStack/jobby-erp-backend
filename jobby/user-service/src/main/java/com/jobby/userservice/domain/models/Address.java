package com.jobby.userservice.domain.models;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class Address {
    private Long id;
    private Municipality municipality;
    private String direction;
    private Instant createdAt;
    private Instant modifiedAt;

    public static Result<Address, Error> create(long id,
                                                Municipality municipality,
                                                String direction){
        return ValidationChain.create()
                .validateNotNull(municipality, "address municipality")
                .validateNotBlank(direction, "address direction")
                .build()
                .map(v -> new Address(id,
                                    municipality,
                                    direction,
                                    Instant.now(),
                                    Instant.now()));
    }

    public static Address reconstruct(long id,
                                      Municipality municipality,
                                      String direction,
                                      Instant createdAt,
                                      Instant modifiedAt){
        return new Address(id, municipality, direction, createdAt, modifiedAt);
    }
}
