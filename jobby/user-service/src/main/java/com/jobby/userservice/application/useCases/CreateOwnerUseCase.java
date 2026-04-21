package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.IdGenerator;
import com.jobby.userservice.application.commands.CreateOwnerCommand;
import com.jobby.userservice.application.commands.CreateUserCommand;
import com.jobby.userservice.application.mapper.CreateOwnerCommandMapper;
import com.jobby.userservice.application.mapper.CreateUserCommandMapper;
import com.jobby.userservice.application.mapper.GetOwnerQueryMapper;
import com.jobby.userservice.application.queries.GetOwnerQuery;
import com.jobby.userservice.domain.ports.IdentificationTypeRepository;
import com.jobby.userservice.domain.ports.OwnerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CreateOwnerUseCase {

    private final OwnerRepository ownerRepository;
    private final IdentificationTypeRepository identificationTypeRepository;
    private final IdGenerator idGenerator;
    private final GetOwnerQueryMapper ownerQueryMapper;
    private final CreateUserCommandMapper createUserCommandMapper;
    private final CreateOwnerCommandMapper createOwnerCommandMapper;

    public Result<GetOwnerQuery, Error> execute(CreateOwnerCommand command) {
        var userCmd = command.getUser();
        return validateUniqueness(userCmd)
                .flatMap(v -> identificationTypeRepository.findById(userCmd.getIdentificationTypeId()))
                .flatMap(identificationType -> idGenerator.next()
                        .flatMap(userId -> createUserCommandMapper.toUser(userCmd, userId, "owner", identificationType)))
                .flatMap(user -> idGenerator.next()
                        .flatMap(ownerId -> createOwnerCommandMapper.toOwner(command, ownerId, user.getId()))
                        .flatMap(owner -> ownerRepository.save(owner, user)
                                .map(v -> ownerQueryMapper.toGetOwnerQuery(owner, user))));
    }

    private Result<Void, Error> validateUniqueness(CreateUserCommand userCmd) {
        return ownerRepository.existByEmail(userCmd.getEmail())
                .flatMap(exist -> exist
                        ? Result.failure(ErrorType.VALIDATION_ERROR, new Field("user", "A user with that email address already exists."))
                        : ownerRepository.existByPhone(userCmd.getPhone()))
                .flatMap(exist -> exist
                        ? Result.failure(ErrorType.VALIDATION_ERROR, new Field("user", "There is already a registered user with that phone number."))
                        : ownerRepository.existByIdentificationNumber(userCmd.getIdentificationNumber()))
                .flatMap(exist -> exist
                        ? Result.failure(ErrorType.VALIDATION_ERROR, new Field("user", "A user with that identification number is already registered."))
                        : Result.success());
    }
}