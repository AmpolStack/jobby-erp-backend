package com.jobby.userservice.infrastructure.adapters.in.responses;

import java.time.Instant;
import java.util.Set;

public record UserResponse(
        Set<ContactResponse> contacts,
        int identificationTypeId,
        String firstName,
        String lastName,
        String role,
        boolean isActive,
        String profileImageUrl,
        String identificationNumber,
        String email,
        String phone,
        Instant createdAt,
        Instant modifiedAt
) {}
