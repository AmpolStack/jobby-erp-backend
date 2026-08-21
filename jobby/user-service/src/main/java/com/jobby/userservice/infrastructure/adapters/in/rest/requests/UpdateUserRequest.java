package com.jobby.userservice.infrastructure.adapters.in.rest.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.mongodb.core.mapping.MongoId;

public record UpdateUserRequest(
        @Min(0)
        int identificationTypeId,
        @NotNull
        @NotBlank
        String identificationNumber,
        @NotNull
        @NotBlank
        @Size(min = 2, max = 150)
        String firstName,
        @NotNull
        @NotBlank
        @Size(min = 2, max = 150)
        String lastName,
        @NotNull
        @NotBlank
        @Size(min = 10, max = 10)
        String phone
) {}