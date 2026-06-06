package com.jobby.userservice.domain.ports.in;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.contract.queries.GetOwnerByIdQuery;
import com.jobby.userservice.domain.contract.responses.OwnerResponse;

public interface GetOwnerByIdUseCase {
    Result<OwnerResponse, Error> execute(GetOwnerByIdQuery query);
}
