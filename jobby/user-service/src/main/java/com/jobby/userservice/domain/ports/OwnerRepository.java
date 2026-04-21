package com.jobby.userservice.domain.ports;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.models.Owner;
import com.jobby.userservice.domain.models.User;

public interface OwnerRepository {
    Result<Void, Error> save(Owner owner, User user);
    Result<Boolean, Error> existByEmail(String email);
    Result<Boolean, Error> existByPhone(String email);
    Result<Boolean, Error> existByIdentificationNumber(String identificationNumber);
}
