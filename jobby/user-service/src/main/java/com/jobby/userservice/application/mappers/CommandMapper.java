package com.jobby.userservice.application.mappers;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.application.contracts.commands.CreateOwnerCommand;
import com.jobby.userservice.application.contracts.commands.CreateUserCommand;
import com.jobby.userservice.domain.models.reference.IdentificationType;
import com.jobby.userservice.domain.models.aggregate.Owner;
import com.jobby.userservice.domain.models.aggregate.User;
import com.jobby.userservice.domain.models.enums.Role;
import com.jobby.userservice.domain.models.vo.shared.Email;
import com.jobby.userservice.domain.models.vo.shared.IdentificationNumber;
import com.jobby.userservice.domain.models.vo.shared.Name;
import com.jobby.userservice.domain.models.vo.shared.Phone;
import org.springframework.stereotype.Component;

@Component
public class CommandMapper {

    public Result<Owner, Error> toOwner(CreateOwnerCommand command,
                                        long ownerId,
                                        long userId) {
        return Owner.create(ownerId,
                userId,
                command.secureParameters());
    }

    public Result<User, Error> toUser(CreateUserCommand command,
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
                        id, command.identificationTypeId(),
                        firstName.data(), lastName.data(),
                        role, idNumber.data(),
                        email.data(),  phone.data()
                ));
    }
}
