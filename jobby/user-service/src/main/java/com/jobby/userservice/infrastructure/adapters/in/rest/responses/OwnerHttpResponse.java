package com.jobby.userservice.infrastructure.adapters.in.rest.responses;

public record OwnerHttpResponse(
        UserHttpResponse user,
        String alternativeEmail
) {}
