package com.jobby.userservice.application.commands;

import com.jobby.userservice.domain.models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateOwnerCommand {
    private CreateUserCommand user;
    private Map<String, String> secureParameters;
}
