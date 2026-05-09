package com.jobby.userservice.infrastructure.adapters.in.requests;


import jakarta.validation.constraints.NotBlank;

public record UpdateRecoveryEmailRequest(
    long id,
    @NotBlank(message = "Email address is required")
    @jakarta.validation.constraints.Email(message = "The recoveryEmail address format is invalid")
    String recoveryEmail
){}
