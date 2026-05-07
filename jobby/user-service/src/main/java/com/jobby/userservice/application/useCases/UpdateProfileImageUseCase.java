package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.TransactionOrchestrator;
import com.jobby.userservice.application.commands.UploadImageCommand;
import com.jobby.userservice.application.mapper.GetUserQueryMapper;
import com.jobby.userservice.application.queries.GetUserQuery;
import com.jobby.userservice.domain.models.User;
import com.jobby.userservice.domain.ports.out.repositories.models.OwnerRepository;
import com.jobby.userservice.domain.ports.out.services.ProfileImageRepository;
import com.jobby.userservice.domain.ports.out.repositories.models.UserRepository;
import com.jobby.userservice.domain.vo.ImageStorageContext;
import com.jobby.userservice.domain.vo.ProfileImage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateProfileImageUseCase {

    private final ProfileImageRepository profileImageRepository;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;

    private final GetUserQueryMapper getUserQueryMapper;
    private final TransactionOrchestrator transaction;

    public Result<GetUserQuery, Error> execute(UploadImageCommand command){
        return this.userRepository.getById(command.userId())
                .flatMap(user -> switch (user.getRole()){
                        case OWNER -> handleOwner(command, user);
                        case EMPLOYEE -> handleEmployee(command, user);
                    });
    }

    private Result<GetUserQuery, Error> handleOwner(UploadImageCommand command, User user) {
        return this.ownerRepository.getByUserId(user.getId())
                .flatMap(owner -> {
                    var context = new ImageStorageContext.OwnerContext(owner.getId());
                    return uploadAndUpdate(command, user, context);
                });
    }

    private Result<GetUserQuery, Error> handleEmployee(UploadImageCommand command, User user) {
        // TODO: Implements real logic
        var context = new ImageStorageContext.EmployeeContext(
        1, 1,1);
        return uploadAndUpdate(command, user, context);
    }


    private Result<GetUserQuery, Error> uploadAndUpdate(
            UploadImageCommand command, User user, ImageStorageContext context) {
        return ProfileImage.of(command.content(), command.contentType(), context)
                .flatMap(profileImageRepository::save)
                .flatMap(newImageUrl -> {

                    if(user.getProfileImageUrl() == null
                            || user.getProfileImageUrl().getValue() == null){
                        return user.updateImageUrl(newImageUrl);
                    }

                    // TODO: Implement a strategy to avoid orphaned files without returning an error
                    return this.profileImageRepository.remove(user.getProfileImageUrl())
                            .flatMap(v -> user.updateImageUrl(newImageUrl));

                })
                .flatMap(v -> userRepository.prepareSave(user))
                .flatMap(task -> transaction.write().add(task).build())
                .map(v -> getUserQueryMapper.toGetUserQuery(user));
    }

}
