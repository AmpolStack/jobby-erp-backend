package com.jobby.userservice.application.commands;

public record UpdateRecoveryEmailCommand(long ownerId, String recoveryEmail){}
