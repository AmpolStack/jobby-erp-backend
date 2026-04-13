package com.jobby.userservice.infrastructure.mappers;

import com.jobby.userservice.domain.models.Owner;
import com.jobby.userservice.infrastructure.persistence.entities.MongoOwnerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface OwnerMapper {

    @Mapping(target = "alternativeEmail", ignore = true)
    Owner toDomain(MongoOwnerEntity entity);

    @Mapping(target = "alternativeEmail", ignore = true)
    @Mapping(target = "alternativeEmailSearchable", ignore = true)
    MongoOwnerEntity toEntity(Owner domain);
}
