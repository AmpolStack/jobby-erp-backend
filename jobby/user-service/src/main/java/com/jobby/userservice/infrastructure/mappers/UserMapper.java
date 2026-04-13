package com.jobby.userservice.infrastructure.mappers;

import com.jobby.userservice.domain.models.User;
import com.jobby.userservice.infrastructure.persistence.entities.MongoUserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ContactMapper.class})
public interface UserMapper {

    @Mapping(target = "identificationNumber", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "profileImageUrl", ignore = true)
    User toDomain(MongoUserEntity entity);

    @Mapping(target = "identificationNumber", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "profileImageUrl", ignore = true)
    @Mapping(target = "identificationNumberSearchable", ignore = true)
    @Mapping(target = "emailSearchable", ignore = true)
    @Mapping(target = "phoneSearchable", ignore = true)
    MongoUserEntity toEntity(User domain);
}
