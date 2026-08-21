package com.jobby.userservice.application.contracts.commands;

import com.jobby.domain.ports.in.Command;

public record ConfirmEmailChangeCommand(long id, String code) implements Command {
}
