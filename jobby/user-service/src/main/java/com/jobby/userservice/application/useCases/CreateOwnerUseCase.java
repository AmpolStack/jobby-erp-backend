package com.jobby.userservice.application.useCases;

import cn.hutool.core.lang.Pair;
import com.jobby.domain.functional.PersistenceTask;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.IdGenerator;
import com.jobby.domain.ports.TransactionOrchestrator;
import com.jobby.userservice.application.commands.CreateOwnerCommand;
import com.jobby.userservice.application.commands.CreateUserCommand;
import com.jobby.userservice.application.mapper.CreateOwnerCommandMapper;
import com.jobby.userservice.application.mapper.CreateUserCommandMapper;
import com.jobby.userservice.application.mapper.GetOwnerQueryMapper;
import com.jobby.userservice.application.queries.GetOwnerQuery;
import com.jobby.userservice.domain.models.Owner;
import com.jobby.userservice.domain.models.User;
import com.jobby.userservice.domain.ports.IdentificationTypeRepository;
import com.jobby.userservice.domain.ports.OwnerRepository;
import com.jobby.userservice.domain.ports.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CreateOwnerUseCase {

    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    private final IdentificationTypeRepository identificationTypeRepository;
    private final IdGenerator idGenerator;
    private final GetOwnerQueryMapper queryMapper;
    private final TransactionOrchestrator transaction;
    private final CreateUserCommandMapper createUserCommandMapper;
    private final CreateOwnerCommandMapper createOwnerCommandMapper;

    public Result<GetOwnerQuery, Error> execute(CreateOwnerCommand command) {
        var userCmd = command.getUser();
        return validateUniqueness(userCmd)
                .flatMap(v -> prepareUser(command.getUser()))
                .flatMap((userTuple) ->
                        prepareOwner(command, userTuple.getValue().getId())
                        .flatMap(ownerTuple ->
                                this.transaction.write()
                                .add(userTuple.getKey())
                                .add(ownerTuple.getKey())
                                .build()
                                .map(v ->
                                        this.queryMapper.toGetOwnerQuery(ownerTuple.getValue(),
                                                userTuple.getValue()))
                        )
                );
    }

    private Result<Void, Error> validateUniqueness(CreateUserCommand userCmd) {
        return this.userRepository.existByEmail(userCmd.getEmail())
                .flatMap(exist -> exist
                        ? Result.failure(ErrorType.VALIDATION_ERROR, new Field("user", "A user with that email address already exists."))
                        : this.userRepository.existByPhone(userCmd.getPhone()))
                .flatMap(exist -> exist
                        ? Result.failure(ErrorType.VALIDATION_ERROR, new Field("user", "There is already a registered user with that phone number."))
                        : this.userRepository.existByIdentificationNumber(userCmd.getIdentificationNumber()))
                .flatMap(exist -> exist
                        ? Result.failure(ErrorType.VALIDATION_ERROR, new Field("user", "A user with that identification number is already registered."))
                        : Result.success());
    }

    private Result<Pair<PersistenceTask, User>,Error> prepareUser(CreateUserCommand cmd){
        return this.identificationTypeRepository.findById(cmd.getIdentificationTypeId())
                .flatMap(identificationType -> this.idGenerator.next()
                        .flatMap(userId ->
                                this.createUserCommandMapper.toUser(cmd, userId, "owner", identificationType))
                .flatMap(user -> this.userRepository.prepareSave(user)
                        .map(task -> Pair.of(task, user)))
                );
    }

    private Result<Pair<PersistenceTask, Owner>,Error> prepareOwner(CreateOwnerCommand cmd, long userId){
        return this.idGenerator.next()
                .flatMap(ownerId -> createOwnerCommandMapper.toOwner(cmd, ownerId, userId))
                .flatMap(owner -> this.ownerRepository.prepareSave(owner)
                        .map(task -> Pair.of(task, owner)));
    }
}