package com.jobby.userservice.domain.contract.commands;

public record ConfirmEmailChangeCommand(long id, String code) {
}
