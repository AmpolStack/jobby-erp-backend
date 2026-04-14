package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.IdGenerator;
import com.jobby.userservice.application.commands.CreateOwnerCommand;
import com.jobby.userservice.application.commands.CreateUserCommand;
import com.jobby.userservice.application.mapper.GetOwnerQueryMapper;
import com.jobby.userservice.application.responses.GetOwnerQuery;
import com.jobby.userservice.domain.factory.OwnerFactory;
import com.jobby.userservice.domain.factory.UserFactory;
import com.jobby.userservice.domain.models.IdentificationType;
import com.jobby.userservice.domain.models.Owner;
import com.jobby.userservice.domain.models.User;
import com.jobby.userservice.domain.ports.IdentificationTypeRepository;
import com.jobby.userservice.domain.ports.OwnerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@AllArgsConstructor
public class CreateOwnerUseCase {

    private final OwnerRepository ownerRepository;
    private final GetOwnerQueryMapper ownerQueryMapper;
    private final IdGenerator idGenerator;
    private final IdentificationTypeRepository identificationTypeRepository;

    public Result<GetOwnerQuery, Error> execute(CreateOwnerCommand command){
        var userCommand = command.getUser();

        return this.identificationTypeRepository
                .findById(userCommand.getIdentificationTypeId())
                .flatMap(identificationType ->
                        this.setupUser(userCommand, identificationType))
                .flatMap(user ->
                        this.setupOwner(command.getSecureParameters(), user))
                .flatMap(this.ownerRepository::create)
                .map(this.ownerQueryMapper::toGetOwnerQuery);
    }


    private Result<User, Error> setupUser(CreateUserCommand userCommand,
                                           IdentificationType identificationType){
        return this.idGenerator.next()
                .flatMap(id -> UserFactory.create(id,
                        userCommand.getIdentificationTypeId(),
                        userCommand.getFirstName(),
                        userCommand.getLastName(),
                        "owner",
                        userCommand.getIdentificationNumber(),
                        identificationType,
                        userCommand.getEmail(),
                        userCommand.getPhone()));
    }

    private Result<Owner, Error> setupOwner(Map<String, String> secureParameters,
                                            User user){
        return this.idGenerator.next()
                .flatMap(id -> OwnerFactory.create(id,
                        null,
                        secureParameters,
                        user));
    }
}
