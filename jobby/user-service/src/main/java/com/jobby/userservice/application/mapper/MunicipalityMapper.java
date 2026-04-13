package com.jobby.userservice.application.mapper;

import com.jobby.userservice.domain.models.Municipality;
import com.jobby.userservice.infrastructure.mappers.DepartmentMapper;
import com.jobby.userservice.infrastructure.persistence.entities.MongoMunicipalityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {DepartmentMapper.class})
public interface MunicipalityMapper {

    MunicipalityMapper INSTANCE = Mappers.getMapper(MunicipalityMapper.class);

    Municipality toDomain(MongoMunicipalityEntity entity);

    MongoMunicipalityEntity toEntity(Municipality domain);
}
