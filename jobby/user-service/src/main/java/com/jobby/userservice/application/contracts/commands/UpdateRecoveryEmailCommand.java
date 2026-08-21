package com.jobby.userservice.application.contracts.commands;

import com.jobby.domain.ports.in.Command;

public record UpdateRecoveryEmailCommand(long ownerId, String recoveryEmail) implements Command {}
