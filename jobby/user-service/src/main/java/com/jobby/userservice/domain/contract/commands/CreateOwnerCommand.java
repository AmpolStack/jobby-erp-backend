package com.jobby.userservice.domain.contract.commands;

import java.util.Map;

public record CreateOwnerCommand(CreateUserCommand user,
                                 Map<String, String> secureParameters) {
}
