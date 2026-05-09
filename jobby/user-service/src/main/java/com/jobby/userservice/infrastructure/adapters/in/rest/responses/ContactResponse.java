package com.jobby.userservice.infrastructure.adapters.in.responses;

public record ContactResponse(
        String name,
        String description,
        boolean isPublic,
        String value
) {}
