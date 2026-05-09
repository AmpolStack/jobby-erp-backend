package com.jobby.userservice.domain.contract.commands;

public record UpdateEmailRequestCommand(int userId,
                                        String newEmail) {}
