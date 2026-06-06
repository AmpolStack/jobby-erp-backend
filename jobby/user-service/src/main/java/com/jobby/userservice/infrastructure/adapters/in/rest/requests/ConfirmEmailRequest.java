package com.jobby.userservice.infrastructure.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;

public record ConfirmEmailRequest(
        long userId,
        @NotBlank
        String code
) { }
