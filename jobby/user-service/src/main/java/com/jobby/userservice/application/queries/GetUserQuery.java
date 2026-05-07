package com.jobby.userservice.application.queries;

import com.jobby.userservice.domain.enums.Role;

import java.time.Instant;
import java.util.Set;

public record GetUserQuery(Set<GetContactQuery> contacts,
                           int identificationTypeId,
                           String firstName,
                           String lastName,
                           Role role,
                           boolean isActive,
                           String profileImageUrl,
                           String identificationNumber,
                           String email,
                           String phone,
                           Instant createdAt,
                           Instant modifiedAt) {
}
