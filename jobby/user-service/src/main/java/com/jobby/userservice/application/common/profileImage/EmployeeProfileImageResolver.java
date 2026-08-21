package com.jobby.userservice.application.common.profileImage;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.application.contracts.commands.UpdateIProfileImageCommand;
import com.jobby.userservice.domain.models.aggregate.User;
import com.jobby.userservice.domain.models.enums.Role;
import com.jobby.userservice.domain.models.vo.ephemeral.ImageStorageContext;
import com.jobby.userservice.application.common.ProfileImageContextResolver;
import org.springframework.stereotype.Component;

@Component
public class EmployeeProfileImageResolver implements ProfileImageContextResolver {

    @Override
    public Role supportedRole() {
        return Role.EMPLOYEE;
    }

    @Override
    public Result<ImageStorageContext, Error> resolve(UpdateIProfileImageCommand command, User user) {
        var context = new ImageStorageContext.EmployeeContext(1, 1, 1);
        return Result.success(context);
    }
}
