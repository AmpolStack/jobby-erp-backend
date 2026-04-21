package com.jobby.userservice.application.mapper;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.application.commands.CreateUserCommand;
import com.jobby.userservice.domain.models.IdentificationType;
import com.jobby.userservice.domain.models.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CreateUserCommandMapper {
    default Result<User, Error> toUser(CreateUserCommand command,
                                       long id,
                                       String role,
                                       IdentificationType identificationType) {
        return User.create(
                id,
                command.getIdentificationTypeId(),
                command.getFirstName(),
                command.getLastName(),
                role,
                command.getIdentificationNumber(),
                identificationType,
                command.getEmail(),
                command.getPhone()
        );
    }
}
