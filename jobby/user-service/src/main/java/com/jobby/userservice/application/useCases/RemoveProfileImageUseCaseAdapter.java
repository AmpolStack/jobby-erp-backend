package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.TransactionOrchestrator;
import com.jobby.userservice.application.mappers.ResponseMapper;
import com.jobby.userservice.domain.contract.commands.RemoveProfileImageCommand;
import com.jobby.userservice.domain.contract.responses.UserResponse;
import com.jobby.userservice.domain.ports.in.RemoveProfileImageUseCase;
import com.jobby.userservice.domain.ports.out.services.StorageService;
import com.jobby.userservice.domain.ports.out.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RemoveProfileImageUseCaseAdapter implements RemoveProfileImageUseCase {
    private final StorageService fileStorageService;
    private final ResponseMapper responseMapper;
    private final UserRepository userRepository;
    private final TransactionOrchestrator transaction;

    public Result<UserResponse, Error> execute(RemoveProfileImageCommand command){
        return this.userRepository.getById(command.userId())
                .flatMap(user -> user.removeProfileImage()
                        .flatMap(this.fileStorageService::removeProfileImage)
                        .flatMap(v -> this.userRepository.prepareSave(user))
                        .flatMap(userTask -> this.transaction.write()
                                .add(userTask)
                                .build())
                        .map(v -> this.responseMapper.toResponse(user)));
    }
}
