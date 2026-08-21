package com.jobby.userservice.application.contracts.commands;

import com.jobby.domain.ports.in.Command;

public record RequestEmailChangeCommand(long userId,
                                        String newEmail) implements Command {}
