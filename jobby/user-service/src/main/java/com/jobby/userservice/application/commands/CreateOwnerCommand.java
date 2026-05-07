package com.jobby.userservice.application.commands;

import java.util.Map;

public record CreateOwnerCommand(CreateUserCommand user,
                                 Map<String, String> secureParameters) {
}
