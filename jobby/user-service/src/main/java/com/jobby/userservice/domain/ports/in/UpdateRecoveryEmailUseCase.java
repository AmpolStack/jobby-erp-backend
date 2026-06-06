package com.jobby.userservice.domain.ports.in;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.contract.commands.UpdateRecoveryEmailCommand;
import com.jobby.userservice.domain.contract.responses.OwnerResponse;

public interface UpdateRecoveryEmailUseCase {
    Result<OwnerResponse, Error> execute(UpdateRecoveryEmailCommand command);
}
