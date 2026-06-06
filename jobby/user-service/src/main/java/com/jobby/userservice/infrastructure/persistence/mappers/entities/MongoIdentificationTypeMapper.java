package com.jobby.userservice.infrastructure.persistence.mappers.entities;

import com.jobby.userservice.domain.models.reference.IdentificationType;
import com.jobby.userservice.infrastructure.persistence.entities.MongoIdentificationTypeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MongoIdentificationTypeMapper {

    default IdentificationType toDomain(MongoIdentificationTypeEntity entity){
        if(entity == null) return null;
        return IdentificationType.reconstruct(entity.getId(),
                entity.getDianCode(),
                entity.getName(),
                entity.getMinLength(),
                entity.getMaxLength(),
                entity.getExpression(),
                entity.getAbbreviation(),
                entity.getAllowCharacters());
    }
}
