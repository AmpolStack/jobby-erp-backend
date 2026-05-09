package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.CacheService;
import com.jobby.domain.ports.TransactionOrchestrator;
import com.jobby.userservice.domain.contract.responses.UserResponse;
import com.jobby.userservice.domain.ports.out.services.FileStorageService;
import com.jobby.userservice.domain.ports.out.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RemoveProfileImageUseCase {
    private final FileStorageService profileImageRepository;
    private final GetUserQueryMapper getUserQueryMapper;
    private final UserRepository userRepository;
    private final TransactionOrchestrator transaction;
    private final CacheService cacheService;

    public Result<UserResponse, Error> execute(long userId){
        return this.userRepository.getById(userId)
                .flatMap(user -> user.removeProfileImage()
                        .flatMap(v -> this.profileImageRepository.remove(user.getProfileImageUrl()))
                        .flatMap(v -> this.userRepository.prepareSave(user))
                        .flatMap(userTask -> this.transaction.write()
                                .add(userTask)
                                .build())
                        .map(v -> this.getUserQueryMapper.toGetUserQuery(user)));
    }
}
