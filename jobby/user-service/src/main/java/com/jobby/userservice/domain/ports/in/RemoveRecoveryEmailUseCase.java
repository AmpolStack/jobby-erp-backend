package com.jobby.userservice.domain.ports.in;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.contract.commands.RemoveRecoveryEmailCommand;
import com.jobby.userservice.domain.contract.responses.OwnerResponse;

public interface RemoveRecoveryEmailUseCase {
    Result<OwnerResponse, Error> execute(RemoveRecoveryEmailCommand command);
}
