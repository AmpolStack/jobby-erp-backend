package com.jobby.userservice.infrastructure.mappers;

import com.jobby.userservice.domain.models.IdentificationType;
import com.jobby.userservice.infrastructure.persistence.entities.MongoIdentificationTypeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IdentificationTypeMapper {

    IdentificationType toDomain(MongoIdentificationTypeEntity entity);

    MongoIdentificationTypeEntity toEntity(IdentificationType domain);
}
