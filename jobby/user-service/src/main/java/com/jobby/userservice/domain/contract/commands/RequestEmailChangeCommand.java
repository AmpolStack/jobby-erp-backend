package com.jobby.userservice.domain.contract.commands;

public record RequestEmailChangeCommand(long userId,
                                        String newEmail) {}
