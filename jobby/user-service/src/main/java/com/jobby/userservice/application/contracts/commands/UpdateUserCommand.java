package com.jobby.userservice.application.contracts.commands;

import com.jobby.domain.ports.in.Command;

public record UpdateUserCommand(
        long userId,
        int identificationTypeId,
        String identificationNumber,
        String firstName,
        String lastName,
        String phone
) implements Command {}
