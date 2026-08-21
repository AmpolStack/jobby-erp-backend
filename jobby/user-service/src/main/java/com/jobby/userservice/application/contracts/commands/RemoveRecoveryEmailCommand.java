package com.jobby.userservice.application.contracts.commands;

import com.jobby.domain.ports.in.Command;

public record RemoveRecoveryEmailCommand(long ownerId) implements Command {
}
