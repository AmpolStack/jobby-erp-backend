package com.jobby.userservice.domain.ports.in;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.contract.commands.RemoveProfileImageCommand;
import com.jobby.userservice.domain.contract.responses.UserResponse;

public interface RemoveProfileImageUseCase {
    Result<UserResponse, Error> execute(RemoveProfileImageCommand command);
}
