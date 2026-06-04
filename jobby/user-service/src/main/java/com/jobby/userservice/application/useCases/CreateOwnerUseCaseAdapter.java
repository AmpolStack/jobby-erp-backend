package com.jobby.userservice.application.useCases;

import cn.hutool.core.lang.Pair;
import com.jobby.domain.functional.PersistenceTask;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.IdGenerator;
import com.jobby.domain.ports.TransactionOrchestrator;
import com.jobby.userservice.application.mappers.CommandMapper;
import com.jobby.userservice.application.mappers.ResponseMapper;
import com.jobby.userservice.domain.contract.commands.CreateOwnerCommand;
import com.jobby.userservice.domain.contract.commands.CreateUserCommand;
import com.jobby.userservice.domain.contract.responses.OwnerResponse;
import com.jobby.userservice.domain.models.aggregate.Owner;
import com.jobby.userservice.domain.models.aggregate.User;
import com.jobby.userservice.domain.ports.in.CreateOwnerUseCase;
import com.jobby.userservice.domain.ports.out.repositories.IdentificationTypeRepository;
import com.jobby.userservice.domain.ports.out.repositories.OwnerRepository;
import com.jobby.userservice.domain.ports.out.repositories.UserRepository;
import com.jobby.userservice.domain.models.enums.Role;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CreateOwnerUseCaseAdapter implements CreateOwnerUseCase {

    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    private final IdentificationTypeRepository identificationTypeRepository;
    private final IdGenerator idGenerator;
    private final CommandMapper commandMapper;
    private final ResponseMapper responseMapper;
    private final TransactionOrchestrator transaction;

    public Result<OwnerResponse, Error> execute(CreateOwnerCommand command) {
        var userCmd = command.user();
        return validateUniqueness(userCmd)
                .flatMap(v -> prepareUser(command.user()))
                .flatMap((userTuple) ->
                        prepareOwner(command, userTuple.getValue().getId())
                        .flatMap(ownerTuple ->
                                this.transaction.write()
                                .add(userTuple.getKey())
                                .add(ownerTuple.getKey())
                                .build()
                                .map(v ->
                                        this.responseMapper.toResponse(ownerTuple.getValue(),
                                                userTuple.getValue()))
                        )
                );
    }

    private Result<Void, Error> validateUniqueness(CreateUserCommand userCmd) {
        return this.userRepository.existByEmail(userCmd.email())
                .flatMap(exist -> exist
                        ? Result.failure(ErrorType.VALIDATION_ERROR, new Field("user", "A user with that recoveryEmail address already exists."))
                        : this.userRepository.existByPhone(userCmd.phone()))
                .flatMap(exist -> exist
                        ? Result.failure(ErrorType.VALIDATION_ERROR, new Field("user", "There is already a registered user with that phone number."))
                        : this.userRepository.existByIdentificationNumber(userCmd.identificationNumber()))
                .flatMap(exist -> exist
                        ? Result.failure(ErrorType.VALIDATION_ERROR, new Field("user", "A user with that identification number is already registered."))
                        : Result.success());
    }

    private Result<Pair<PersistenceTask, User>,Error> prepareUser(CreateUserCommand cmd){
        return this.identificationTypeRepository.findById(cmd.identificationTypeId())
                .flatMap(identificationType -> this.idGenerator.next()
                        .flatMap(userId ->
                                this.commandMapper.toUser(cmd, userId, Role.OWNER, identificationType))
                .flatMap(user -> this.userRepository.prepareSave(user)
                        .map(task -> Pair.of(task, user)))
                );
    }

    private Result<Pair<PersistenceTask, Owner>,Error> prepareOwner(CreateOwnerCommand cmd, long userId){
        return this.idGenerator.next()
                .flatMap(ownerId -> this.commandMapper.toOwner(cmd, ownerId, userId))
                .flatMap(owner -> this.ownerRepository.prepareSave(owner)
                        .map(task -> Pair.of(task, owner)));
    }
}