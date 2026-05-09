package com.jobby.userservice.domain.commands;


public record CreateUserCommand(int identificationTypeId,
                                long organizationId,
                                long sectionalId,
                                String firstName,
                                String lastName,
                                String identificationNumber,
                                String email,
                                String phone) {
}
