package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.TransactionOrchestrator;
import com.jobby.domain.ports.in.CommandHandler;
import com.jobby.userservice.application.mappers.ResponseMapper;
import com.jobby.userservice.application.contracts.commands.UpdateRecoveryEmailCommand;
import com.jobby.userservice.application.responses.OwnerResponse;
import com.jobby.userservice.domain.ports.out.repositories.OwnerRepository;
import com.jobby.userservice.domain.models.aggregate.Owner;
import com.jobby.userservice.domain.ports.out.repositories.UserRepository;
import com.jobby.userservice.domain.models.vo.shared.Email;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateRecoveryEmailUseCaseAdapter implements CommandHandler<UpdateRecoveryEmailCommand, OwnerResponse> {

    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    private final TransactionOrchestrator transaction;
    private final ResponseMapper responseMapper;

    public Result<OwnerResponse, Error> execute(UpdateRecoveryEmailCommand command){
        return Email.of(command.recoveryEmail())
                .flatMap(email -> this.ownerRepository.getById(command.ownerId())
                        .flatMap(owner -> this.userRepository.getById(owner.getUserId())
                                .flatMap(user -> isEmailNotPrimary(owner, email)
                                        .flatMap(v -> owner.updateRecoveryEmail(email))
                                        .flatMap(v -> this.ownerRepository.prepareSave(owner))
                                        .flatMap(task -> this.transaction.write()
                                                .add(task)
                                                .build())
                                        .map(v -> this.responseMapper.toResponse(owner, user)))));
    }

    private Result<Void, Error> isEmailNotPrimary(Owner owner, Email email) {
        return this.userRepository.existByIdAndEmail(owner.getUserId(), email.getEmail())
                .flatMap(exist -> exist
                        ? Result.failure(ErrorType.RESOURCE_ALREADY_EXISTS,
                                new Field("owner", "Your recovery recoveryEmail cannot be the same as your primary recoveryEmail."))
                        : Result.success());
    }
}
