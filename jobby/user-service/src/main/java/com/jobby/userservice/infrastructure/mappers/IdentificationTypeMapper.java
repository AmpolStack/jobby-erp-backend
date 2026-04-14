package com.jobby.userservice.infrastructure.mappers;

import com.jobby.userservice.domain.models.IdentificationType;
import com.jobby.userservice.infrastructure.persistence.entities.MongoIdentificationTypeEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IdentificationTypeMapper {

    IdentificationType toDomain(MongoIdentificationTypeEntity entity);

    List<IdentificationType> toDomain(List<MongoIdentificationTypeEntity> entity);

    MongoIdentificationTypeEntity toEntity(IdentificationType domain);
}
