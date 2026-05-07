package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.TransactionOrchestrator;
import com.jobby.userservice.application.commands.UpdateRecoveryEmailCommand;
import com.jobby.userservice.application.mapper.GetOwnerQueryMapper;
import com.jobby.userservice.application.queries.GetOwnerQuery;
import com.jobby.userservice.domain.models.Owner;
import com.jobby.userservice.domain.ports.out.repositories.models.OwnerRepository;
import com.jobby.userservice.domain.ports.out.repositories.models.UserRepository;
import com.jobby.userservice.domain.vo.Email;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateRecoveryEmailUseCase {

    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    private final TransactionOrchestrator transaction;
    private final GetOwnerQueryMapper getOwnerQueryMapper;

    public Result<GetOwnerQuery, Error> execute(UpdateRecoveryEmailCommand command){
        return Email.of(command.recoveryEmail())
                .flatMap(email -> this.ownerRepository.getById(command.ownerId())
                        .flatMap(owner -> updateEmail(email, owner)
                                .flatMap(v -> this.userRepository.getById(owner.getUserId())
                                .map(user -> this.getOwnerQueryMapper.toGetOwnerQuery(owner, user)))));
    }

    private Result<Void, Error> updateEmail(Email email, Owner owner)
    {
        return this.userRepository.existByIdAndEmail(owner.getUserId(), email.getEmail())
                        .flatMap(exist ->
                                        (exist == false) ? owner.updateRecoveryEmail(email)
                                        : Result.failure(ErrorType.RESOURCE_ALREADY_EXISTS,
                                                new Field("owner",
                                                        "Your recovery recoveryEmail cannot be the same as your primary recoveryEmail."))
                                )
                .flatMap(v -> this.ownerRepository.prepareSave(owner))
                .flatMap(task -> this.transaction.write()
                        .add(task)
                        .build());
    }
}
