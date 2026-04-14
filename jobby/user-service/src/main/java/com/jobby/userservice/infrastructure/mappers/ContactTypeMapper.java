package com.jobby.userservice.infrastructure.mappers;

import com.jobby.userservice.domain.models.ContactType;
import com.jobby.userservice.infrastructure.persistence.entities.MongoContactTypeEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContactTypeMapper {

    ContactType toDomain(MongoContactTypeEntity entity);

    List<ContactType> toDomain(List<MongoContactTypeEntity> entity);

    MongoContactTypeEntity toEntity(ContactType domain);
}
