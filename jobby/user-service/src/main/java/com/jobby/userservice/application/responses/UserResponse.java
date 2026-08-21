package com.jobby.userservice.application.responses;

import com.jobby.userservice.domain.models.enums.Role;

import java.time.Instant;
import java.util.Set;

public record UserResponse(Set<ContactResponse> contacts,
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
