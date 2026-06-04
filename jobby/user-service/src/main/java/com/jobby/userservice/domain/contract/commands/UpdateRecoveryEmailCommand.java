package com.jobby.userservice.domain.contract.commands;

public record UpdateRecoveryEmailCommand(long ownerId, String recoveryEmail){}
