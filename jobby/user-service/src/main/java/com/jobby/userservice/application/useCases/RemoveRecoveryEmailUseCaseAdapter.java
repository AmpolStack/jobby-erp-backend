package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.TransactionOrchestrator;
import com.jobby.userservice.application.mappers.ResponseMapper;
import com.jobby.userservice.domain.contract.commands.RemoveRecoveryEmailCommand;
import com.jobby.userservice.domain.contract.responses.OwnerResponse;
import com.jobby.userservice.domain.ports.in.RemoveRecoveryEmailUseCase;
import com.jobby.userservice.domain.ports.out.repositories.OwnerRepository;
import com.jobby.userservice.domain.ports.out.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RemoveRecoveryEmailUseCaseAdapter implements RemoveRecoveryEmailUseCase {

    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    private final TransactionOrchestrator transaction;
    private final ResponseMapper responseMapper;

    public Result<OwnerResponse, Error> execute(RemoveRecoveryEmailCommand command){
        return this.ownerRepository.getById(command.ownerId())
                .flatMap(owner -> this.userRepository.getById(owner.getUserId())
                        .flatMap(user -> owner.removeRecoveryEmail()
                                .flatMap(v -> this.ownerRepository.prepareSave(owner))
                                .flatMap(task -> this.transaction.write()
                                        .add(task)
                                        .build())
                                .map(v -> this.responseMapper.toResponse(owner, user))));
    }
}
