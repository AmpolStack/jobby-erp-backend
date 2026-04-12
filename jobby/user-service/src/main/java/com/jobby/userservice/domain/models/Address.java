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
public class Address {
    private Long id;
    private Municipality municipality;
    private String direction;
    private Instant createdAt;
    private Instant modifiedAt;
}
