package com.jobby.userservice.domain.ports;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.models.ContactType;
import java.util.List;

public interface ContactTypeRepository {
    Result<List<ContactType>, Error> findAll();
    Result<ContactType, Error> findById(int id);
}
