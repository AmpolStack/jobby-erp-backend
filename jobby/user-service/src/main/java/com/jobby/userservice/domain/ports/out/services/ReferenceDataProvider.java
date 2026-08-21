package com.jobby.userservice.domain.ports.out.services;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.models.reference.ContactType;
import com.jobby.userservice.domain.models.reference.Department;
import com.jobby.userservice.domain.models.reference.IdentificationType;
import com.jobby.userservice.domain.models.reference.Municipality;

public interface ReferenceDataProvider {
    Result<IdentificationType, Error> identificationType(int id);
    Result<ContactType, Error> contactType(int id);
    Result<Municipality, Error> municipality(int id);
    Result<Department, Error> department(int id);
}
