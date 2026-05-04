package com.jobby.userservice.infrastructure.mappers.entities;

import com.jobby.userservice.domain.models.Employee;
import com.jobby.userservice.infrastructure.persistence.entities.MongoEmployeeEntity;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {MongoAddressMapper.class})
public abstract class EmployeeMapper {

    @Autowired
    protected MongoAddressMapper mongoAddressMapper;

    public Employee toDomain(MongoEmployeeEntity entity){
        if(entity == null) return null;
        return Employee.reconstruct(entity.getId(),
                this.mongoAddressMapper.toDomain(entity.getAddress()),
                entity.getSectionalId(),
                entity.getPositionName(),
                entity.getCreatedAt(),
                entity.getModifiedAt());
    }

    public abstract MongoEmployeeEntity toEntity(Employee domain);
}
