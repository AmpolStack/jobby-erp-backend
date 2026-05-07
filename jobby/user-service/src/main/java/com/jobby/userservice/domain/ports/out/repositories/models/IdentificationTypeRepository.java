package com.jobby.userservice.domain.ports.out.repositories.models;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.models.IdentificationType;
import java.util.List;

public interface IdentificationTypeRepository {
    Result<IdentificationType, Error> findById(int id);
    Result<List<IdentificationType>, Error> getAll();
}
