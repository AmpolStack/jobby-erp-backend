package com.jobby.userservice.application.contracts.commands;

import com.jobby.domain.ports.in.Command;
import java.util.Map;

public record CreateOwnerCommand(CreateUserCommand user,
                                 Map<String, String> secureParameters) implements Command {
}
