package com.jobby.userservice.infrastructure.mappers.entities;

import com.jobby.userservice.domain.models.Department;
import com.jobby.userservice.infrastructure.persistence.entities.MongoDepartmentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MongoDepartmentMapper {

    default Department toDomain(MongoDepartmentEntity entity){
        if(entity == null) return null;
        return Department.reconstruct(entity.getId(),
                entity.getName(),
                entity.getDaneCode());
    }

    MongoDepartmentEntity toEntity(Department domain);
}
