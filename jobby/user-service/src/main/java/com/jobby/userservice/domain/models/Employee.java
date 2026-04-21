package com.jobby.userservice.domain.models;

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
