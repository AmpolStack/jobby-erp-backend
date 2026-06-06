package com.jobby.userservice.domain.ports.in;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.contract.commands.UpdateIProfileImageCommand;
import com.jobby.userservice.domain.models.aggregate.User;
import com.jobby.userservice.domain.models.enums.Role;
import com.jobby.userservice.domain.models.vo.ephemeral.ImageStorageContext;

public interface ProfileImageContextResolver {
    Role supportedRole();
    Result<ImageStorageContext, Error> resolve(UpdateIProfileImageCommand command, User user);
}
