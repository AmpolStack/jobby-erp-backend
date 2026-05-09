package com.jobby.userservice.domain.models;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Employee {
    private Long id;
    private Address address;
    private int sectionalId;
    private String positionName;
    private Instant createdAt;
    private Instant modifiedAt;

    public static Result<Employee, Error> create(long id,
                                                 Address address,
                                                 int sectionalId,
                                                 String positionName) {
        return ValidationChain.create()
                .validateNotNull(address, "address")
                .validateNotBlank(positionName, "position name")
                .build()
                .map(v -> new Employee(id, address, sectionalId, positionName,
                        Instant.now(), Instant.now()));
    }

    public static Employee reconstruct(long id,
                                Address address,
                                int sectionalId,
                                String positionName,
                                Instant createdAt,
                                Instant modifiedAt){
        return new Employee(id,
                address,
                sectionalId,
                positionName,
                createdAt,
                modifiedAt);
    }
}

