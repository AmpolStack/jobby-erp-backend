package com.jobby.userservice.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    private Long id;
    private User user;
    private Address address;
    private int sectionalId;
    private String positionName;
    private Instant createdAt;
    private Instant modifiedAt;
}
