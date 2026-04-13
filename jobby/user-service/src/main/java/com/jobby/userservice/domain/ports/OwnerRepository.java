package com.jobby.userservice.domain.ports;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.models.Owner;
import com.jobby.userservice.domain.models.User;

import java.util.List;
import java.util.Optional;

public interface OwnerRepository {
    Result<Owner, Error> create(Owner owner);
}
