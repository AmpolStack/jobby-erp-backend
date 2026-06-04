package com.jobby.userservice.infrastructure.adapters.in.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CreateOwnerRequest(
        @Valid
        @NotNull(message = "User data is required")
        CreateUserRequest user,
        long organizationId,
        Map<String, String> secureParameters
) {}
