package com.jobby.userservice.application.commands;

public record UpdateEmailCommand(int userId,
                                 String newEmail) {}
