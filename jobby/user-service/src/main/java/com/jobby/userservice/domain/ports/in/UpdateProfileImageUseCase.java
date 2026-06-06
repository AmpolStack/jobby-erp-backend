package com.jobby.userservice.domain.ports.in;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.contract.commands.UpdateIProfileImageCommand;
import com.jobby.userservice.domain.contract.responses.UserResponse;

public interface UpdateProfileImageUseCase {
    Result<UserResponse, Error> execute(UpdateIProfileImageCommand command);
}
