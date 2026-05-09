package com.jobby.userservice.domain.commands;

import java.util.Map;

public record CreateOwnerCommand(CreateUserCommand user,
                                 Map<String, String> secureParameters) {
}
