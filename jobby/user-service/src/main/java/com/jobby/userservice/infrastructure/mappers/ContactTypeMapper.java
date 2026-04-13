package com.jobby.userservice.infrastructure.mappers;

import com.jobby.userservice.domain.models.ContactType;
import com.jobby.userservice.infrastructure.persistence.entities.MongoContactTypeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContactTypeMapper {

    ContactType toDomain(MongoContactTypeEntity entity);

    MongoContactTypeEntity toEntity(ContactType domain);
}
