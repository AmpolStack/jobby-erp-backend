package com.jobby.userservice.infrastructure.adapters.in.rest.responses;

public record OwnerResponse(
        UserResponse user,
        String alternativeEmail
) {}
