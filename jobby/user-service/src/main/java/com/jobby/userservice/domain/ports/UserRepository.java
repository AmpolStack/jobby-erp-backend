package com.jobby.userservice.domain.ports;

import com.jobby.domain.functional.PersistenceTask;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.models.User;

public interface UserRepository {
    Result<PersistenceTask, Error> prepareSave(User user);
    Result<Boolean, Error> existByEmail(String email);
    Result<Boolean, Error> existByPhone(String email);
    Result<Boolean, Error> existByIdentificationNumber(String identificationNumber);
}
