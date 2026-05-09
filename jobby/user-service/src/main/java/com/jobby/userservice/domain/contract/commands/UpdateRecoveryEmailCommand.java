package com.jobby.userservice.domain.commands;

public record UpdateRecoveryEmailCommand(long ownerId, String recoveryEmail){}
