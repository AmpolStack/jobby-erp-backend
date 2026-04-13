package com.jobby.userservice.domain.models;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class Employee {
    private Long id;
    private Address address;
    private int sectionalId;
    private String positionName;
    private Instant createdAt;
    private Instant modifiedAt;
}
