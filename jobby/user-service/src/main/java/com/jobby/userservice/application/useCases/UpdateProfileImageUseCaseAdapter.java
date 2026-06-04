package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.TransactionOrchestrator;
import com.jobby.userservice.application.mappers.ResponseMapper;
import com.jobby.userservice.domain.contract.commands.UpdateIProfileImageCommand;
import com.jobby.userservice.domain.contract.responses.UserResponse;
import com.jobby.userservice.domain.models.aggregate.User;
import com.jobby.userservice.domain.ports.in.UpdateProfileImageUseCase;
import com.jobby.userservice.domain.ports.out.repositories.OwnerRepository;
import com.jobby.userservice.domain.ports.out.services.StorageService;
import com.jobby.userservice.domain.ports.out.repositories.UserRepository;
import com.jobby.userservice.domain.models.vo.ephemeral.ImageStorageContext;
import com.jobby.userservice.domain.models.vo.ephemeral.ProfileImage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateProfileImageUseCaseAdapter implements UpdateProfileImageUseCase {

    private final StorageService fileStorageService;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;

    private final ResponseMapper responseMapper;
    private final TransactionOrchestrator transaction;

    public Result<UserResponse, Error> execute(UpdateIProfileImageCommand command){
        return this.userRepository.getById(command.userId())
                .flatMap(user -> switch (user.getRole()){
                        case OWNER -> handleOwner(command, user);
                        case EMPLOYEE -> handleEmployee(command, user);
                    });
    }

    private Result<UserResponse, Error> handleOwner(UpdateIProfileImageCommand command, User user) {
        return this.ownerRepository.getByUserId(user.getId())
                .flatMap(owner -> {
                    var context = new ImageStorageContext.OwnerContext(owner.getId());
                    return uploadAndUpdate(command, user, context);
                });
    }

    private Result<UserResponse, Error> handleEmployee(UpdateIProfileImageCommand command, User user) {
        // TODO: Implements real logic
        var context = new ImageStorageContext.EmployeeContext(
        1, 1,1);
        return uploadAndUpdate(command, user, context);
    }


    private Result<UserResponse, Error> uploadAndUpdate(
            UpdateIProfileImageCommand command, User user, ImageStorageContext context) {
        return ProfileImage.of(command.content(), command.contentType(), context)
                .flatMap(this.fileStorageService::changeProfileImage)
                .flatMap(newImageUrl -> {

                    if(user.getProfileImageUrl() == null
                            || user.getProfileImageUrl().getValue() == null){
                        return user.updateImageUrl(newImageUrl);
                    }

                    // TODO: Implement a strategy to avoid orphaned files without returning an error
                    return this.fileStorageService.removeProfileImage(user.getProfileImageUrl())
                            .flatMap(v -> user.updateImageUrl(newImageUrl));

                })
                .flatMap(v -> this.userRepository.prepareSave(user))
                .flatMap(task -> this.transaction.write().add(task).build())
                .map(v -> this.responseMapper.toResponse(user));
    }

}
