package com.jobby.userservice.application.contracts.commands;

import com.jobby.domain.ports.in.Command;

public record UpdateUserStatusCommand(
        boolean isActive,
        long userId) implements Command {}
