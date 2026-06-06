package com.jobby.userservice.domain.ports.in;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.contract.commands.ConfirmEmailChangeCommand;
import com.jobby.userservice.domain.contract.commands.RequestEmailChangeCommand;
import com.jobby.userservice.domain.contract.responses.UserResponse;

public interface UpdateEmailUseCase {
    Result<Void, Error> request(RequestEmailChangeCommand command);
    Result<UserResponse, Error> confirm(ConfirmEmailChangeCommand command);
}
