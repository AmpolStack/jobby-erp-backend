package com.jobby.userservice.application.common.profileImage;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.application.contracts.commands.UpdateIProfileImageCommand;
import com.jobby.userservice.domain.models.aggregate.User;
import com.jobby.userservice.domain.models.enums.Role;
import com.jobby.userservice.domain.models.vo.ephemeral.ImageStorageContext;
import com.jobby.userservice.application.common.ProfileImageContextResolver;
import com.jobby.userservice.domain.ports.out.repositories.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OwnerProfileImageResolver implements ProfileImageContextResolver {

    private final OwnerRepository ownerRepository;

    @Override
    public Role supportedRole() {
        return Role.OWNER;
    }

    @Override
    public Result<ImageStorageContext, Error> resolve(UpdateIProfileImageCommand command, User user) {
        return this.ownerRepository.getByUserId(user.getId())
                .map(owner -> new ImageStorageContext.OwnerContext(owner.getId()));
    }
}
