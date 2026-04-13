package com.jobby.userservice.application.mapper;

import com.jobby.userservice.application.responses.GetUserQuery;
import com.jobby.userservice.domain.models.User;
import com.jobby.userservice.infrastructure.mappers.ContactMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {ContactMapper.class})
public interface GetUserQueryMapper {

    GetUserQueryMapper INSTANCE = Mappers.getMapper(GetUserQueryMapper.class);

    @Mapping(source = "profileImageUrl.value", target = "profileImageUrl")
    @Mapping(source = "identificationNumber.number", target = "identificationNumber")
    @Mapping(source = "email.email", target = "email")
    @Mapping(source = "phone.number", target = "phone")
    GetUserQuery toGetUserQuery(User user);
}
