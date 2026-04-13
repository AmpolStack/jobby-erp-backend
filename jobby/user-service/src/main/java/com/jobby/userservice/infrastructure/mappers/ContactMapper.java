package com.jobby.userservice.infrastructure.mappers;

import com.jobby.userservice.domain.models.Contact;
import com.jobby.userservice.infrastructure.persistence.entities.MongoContactEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContactMapper {

    @Mapping(source = "value", target = "contactValue.value")
    Contact toDomain(MongoContactEntity entity);

    @Mapping(source = "contactValue.value", target = "value")
    MongoContactEntity toEntity(Contact domain);
}
