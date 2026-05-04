package com.jobby.userservice.infrastructure.mappers.entities;

import com.jobby.userservice.domain.models.Municipality;
import com.jobby.userservice.infrastructure.persistence.entities.MongoMunicipalityEntity;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {MongoDepartmentMapper.class})
public abstract class MongoMunicipalityMapper {

    @Autowired
    protected MongoDepartmentMapper mongoDepartmentMapper;

    public Municipality toDomain(MongoMunicipalityEntity entity){
        if(entity == null) return null;
        return Municipality.reconstruct(entity.getId(),
                mongoDepartmentMapper.toDomain(entity.getDepartment()),
                entity.getName(),
                entity.getDaneCode());
    }

    public abstract MongoMunicipalityEntity toEntity(Municipality domain);
}
