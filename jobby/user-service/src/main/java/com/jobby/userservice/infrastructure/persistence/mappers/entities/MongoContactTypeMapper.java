package com.jobby.userservice.infrastructure.mappers.entities;

import com.jobby.userservice.domain.models.ContactType;
import com.jobby.userservice.infrastructure.persistence.entities.MongoContactTypeEntity;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MongoContactTypeMapper {

    default ContactType toDomain(MongoContactTypeEntity entity){
        if(entity == null) return null;
        return ContactType.reconstruct(entity.getId(),
                entity.getType(),
                entity.getDescription(),
                entity.getExpression(),
                entity.getVisualInstructions());
    }

    List<ContactType> toDomain(List<MongoContactTypeEntity> entity);

    MongoContactTypeEntity toEntity(ContactType domain);
}
