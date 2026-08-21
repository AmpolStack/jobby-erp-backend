package com.jobby.userservice.application.contracts.commands;

import com.jobby.domain.ports.in.Command;

public record CreateUserCommand(int identificationTypeId,
                                String firstName,
                                String lastName,
                                String identificationNumber,
                                String email,
                                String phone) implements Command {
}
