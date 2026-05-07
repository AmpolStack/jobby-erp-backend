package com.jobby.userservice.domain.ports.out.repositories.models;

import com.jobby.domain.functional.PersistenceTask;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.models.Owner;

public interface OwnerRepository {
    Result<PersistenceTask, Error> prepareSave(Owner owner);
    Result<Owner, Error> getById(long id);
    Result<Owner, Error> getByUserId(long userId);
}
