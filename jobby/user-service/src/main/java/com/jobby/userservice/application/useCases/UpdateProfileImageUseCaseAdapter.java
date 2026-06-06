package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.TransactionOrchestrator;
import com.jobby.userservice.application.mappers.ResponseMapper;
import com.jobby.userservice.domain.contract.commands.UpdateIProfileImageCommand;
import com.jobby.userservice.domain.contract.responses.UserResponse;
import com.jobby.userservice.domain.models.aggregate.User;
import com.jobby.userservice.domain.models.enums.Role;
import com.jobby.userservice.domain.models.vo.ephemeral.ImageStorageContext;
import com.jobby.userservice.domain.models.vo.ephemeral.ProfileImage;
import com.jobby.userservice.domain.ports.in.ProfileImageContextResolver;
import com.jobby.userservice.domain.ports.in.UpdateProfileImageUseCase;
import com.jobby.userservice.domain.ports.out.repositories.UserRepository;
import com.jobby.userservice.domain.ports.out.services.StorageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UpdateProfileImageUseCaseAdapter implements UpdateProfileImageUseCase {

    private final StorageService fileStorageService;
    private final UserRepository userRepository;
    private final ResponseMapper responseMapper;
    private final TransactionOrchestrator transaction;
    private final Map<Role, ProfileImageContextResolver> resolvers;

    public UpdateProfileImageUseCaseAdapter(
            StorageService fileStorageService,
            UserRepository userRepository,
            ResponseMapper responseMapper,
            TransactionOrchestrator transaction,
            List<ProfileImageContextResolver> resolverList) {
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.responseMapper = responseMapper;
        this.transaction = transaction;
        this.resolvers = resolverList.stream()
                .collect(Collectors.toMap(
                        ProfileImageContextResolver::supportedRole,
                        Function.identity()));
    }

    public Result<UserResponse, Error> execute(UpdateIProfileImageCommand command) {
        return this.userRepository.getById(command.userId())
                .flatMap(user -> resolveContext(command, user)
                        .flatMap(context -> uploadAndUpdate(command, user, context)));
    }

    private Result<ImageStorageContext, Error> resolveContext(
            UpdateIProfileImageCommand command, User user) {
        var resolver = resolvers.get(user.getRole());
        if (resolver == null) {
            return Result.failure(ErrorType.VALIDATION_ERROR,
                    new Field("role", "No profile image handler for role: " + user.getRole()));
        }
        return resolver.resolve(command, user);
    }

    private Result<UserResponse, Error> uploadAndUpdate(
            UpdateIProfileImageCommand command, User user, ImageStorageContext context) {
        return ProfileImage.of(command.content(), command.contentType(), context)
                .flatMap(this.fileStorageService::changeProfileImage)
                .flatMap(user::replaceImage)
                .flatMap(result -> result.hasOldImage()
                        ? this.fileStorageService.removeProfileImage(result.oldImageUrl())
                        : Result.success())
                .flatMap(v -> this.userRepository.prepareSave(user))
                .flatMap(task -> this.transaction.write().add(task).build())
                .map(v -> this.responseMapper.toResponse(user));
    }
}
