package com.jobby.userservice.application.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserCommand {
    private int identificationTypeId;
    private String firstName;
    private String lastName;
    private String identificationNumber;
    private String email;
    private String phone;
}
