package com.jobby.userservice.application.useCases;

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
import com.jobby.userservice.domain.models.enums.Role;
import com.jobby.userservice.domain.ports.in.CreateOwnerUseCase;
import com.jobby.userservice.domain.ports.out.repositories.OwnerRepository;
import com.jobby.userservice.domain.ports.out.repositories.UserRepository;
import com.jobby.userservice.domain.ports.out.services.ReferenceDataProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CreateOwnerUseCaseAdapter implements CreateOwnerUseCase {

    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    private final ReferenceDataProvider referenceData;
    private final IdGenerator idGenerator;
    private final CommandMapper commandMapper;
    private final ResponseMapper responseMapper;
    private final TransactionOrchestrator transaction;

    public Result<OwnerResponse, Error> execute(CreateOwnerCommand command) {
        var userCmd = command.user();
        return validateUniqueness(userCmd)
                .flatMap(v -> prepareUser(command.user()))
                .flatMap(user -> prepareOwner(command, user.getId())
                        .flatMap(owner -> executeTransaction(user, owner)));
    }

    private Result<Void, Error> validateUniqueness(CreateUserCommand userCmd) {
        return this.userRepository.existByEmail(userCmd.email())
                .flatMap(exist -> exist
                        ? Result.failure(ErrorType.VALIDATION_ERROR,
                        new Field("user", "A user with that recoveryEmail address already exists."))
                        : this.userRepository.existByPhone(userCmd.phone()))
                .flatMap(exist -> exist
                        ? Result.failure(ErrorType.VALIDATION_ERROR,
                        new Field("user", "There is already a registered user with that phone number."))
                        : this.userRepository.existByIdentificationNumber(userCmd.identificationNumber()))
                .flatMap(exist -> exist
                        ? Result.failure(ErrorType.VALIDATION_ERROR,
                        new Field("user", "A user with that identification number is already registered."))
                        : Result.success());
    }

    private Result<User, Error> prepareUser(CreateUserCommand cmd) {
        return this.referenceData.identificationType(cmd.identificationTypeId())
                .flatMap(identificationType -> this.idGenerator.next()
                        .flatMap(userId -> this.commandMapper.toUser(cmd, userId, Role.OWNER, identificationType)));
    }

    private Result<Owner, Error> prepareOwner(CreateOwnerCommand cmd, long userId) {
        return this.idGenerator.next()
                .flatMap(ownerId -> this.commandMapper.toOwner(cmd, ownerId, userId));
    }

    private Result<OwnerResponse, Error> executeTransaction(User user, Owner owner) {
        return this.userRepository.prepareSave(user)
                .flatMap(userTask -> this.ownerRepository.prepareSave(owner)
                        .flatMap(ownerTask -> this.transaction.write()
                                .add(userTask)
                                .add(ownerTask)
                                .build()
                                .map(v -> this.responseMapper.toResponse(owner, user))));
    }
}
