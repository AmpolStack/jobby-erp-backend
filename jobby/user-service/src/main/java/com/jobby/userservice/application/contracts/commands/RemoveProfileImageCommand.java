package com.jobby.userservice.application.contracts.commands;

import com.jobby.domain.ports.in.Command;

public record RemoveProfileImageCommand(long userId) implements Command {
}
