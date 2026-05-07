package com.jobby.userservice.application.mapper;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.application.commands.CreateUserCommand;
import com.jobby.userservice.domain.enums.Role;
import com.jobby.userservice.domain.models.IdentificationType;
import com.jobby.userservice.domain.models.User;
import com.jobby.userservice.domain.vo.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CreateUserCommandMapper {

    default Result<User, Error> toUser(CreateUserCommand command,
                                       long id,
                                       Role role,
                                       IdentificationType identificationType) {

        var firstName    = Name.of(command.firstName(), "firstName");
        var lastName     = Name.of(command.lastName(), "lastName");
        var idNumber     = IdentificationNumber.of(command.identificationNumber(), identificationType);
        var email        = Email.of(command.email());
        var phone        = Phone.of(command.phone());
        return ValidationChain.create()
                .add(firstName)
                .add(lastName)
                .add(idNumber)
                .add(email)
                .add(phone)
                .build()
                .flatMap(v -> User.create(
                        id,
                        command.identificationTypeId(),
                        Name.reconstruct(command.firstName()),
                        Name.reconstruct(command.lastName()),
                        role,
                        IdentificationNumber.reconstruct(command.identificationNumber()),
                        Email.reconstruct(command.email()),
                        Phone.reconstruct(command.phone())
                ));
    }
}

