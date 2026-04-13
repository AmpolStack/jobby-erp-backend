package com.jobby.userservice.infrastructure.mappers;

import com.jobby.userservice.domain.models.Department;
import com.jobby.userservice.infrastructure.persistence.entities.MongoDepartmentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    Department toDomain(MongoDepartmentEntity entity);

    MongoDepartmentEntity toEntity(Department domain);
}
