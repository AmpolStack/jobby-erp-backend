package com.jobby.userservice.infrastructure.adapters.in.responses;

public record OwnerResponse(
        UserResponse user,
        String alternativeEmail
) {}
