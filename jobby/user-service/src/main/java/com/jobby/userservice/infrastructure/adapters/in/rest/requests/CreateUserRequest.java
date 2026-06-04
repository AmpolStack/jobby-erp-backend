package com.jobby.userservice.infrastructure.adapters.in.rest.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        long sectionalId,

        @Min(value = 1, message = "Identification type ID must be a positive number")
        int identificationTypeId,

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 150, message = "First name must be between 2 and 150 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 150, message = "Last name must be between 2 and 150 characters")
        String lastName,

        @NotBlank(message = "Identification number is required")
        String identificationNumber,

        @NotBlank(message = "Email address is required")
        @jakarta.validation.constraints.Email(message = "The recoveryEmail address format is invalid")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^3\\d{9}$", message = "The phone number format is invalid")
        String phone
) {}
