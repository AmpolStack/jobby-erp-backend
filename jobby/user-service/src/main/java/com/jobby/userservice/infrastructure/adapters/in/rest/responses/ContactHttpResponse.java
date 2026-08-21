package com.jobby.userservice.infrastructure.adapters.in.rest.responses;

public record ContactHttpResponse(
        String name,
        String description,
        boolean isPublic,
        String value
) {}
