package com.jobby.userservice.infrastructure.mappers;

import com.jobby.userservice.domain.models.Employee;
import com.jobby.userservice.infrastructure.persistence.entities.MongoEmployeeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface EmployeeMapper {

    Employee toDomain(MongoEmployeeEntity entity);

    MongoEmployeeEntity toEntity(Employee domain);
}
