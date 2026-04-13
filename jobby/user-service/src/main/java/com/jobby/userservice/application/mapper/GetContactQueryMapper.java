package com.jobby.userservice.application.mapper;

import com.jobby.userservice.application.responses.GetContactQuery;
import com.jobby.userservice.domain.models.Contact;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GetContactQueryMapper {

    GetContactQueryMapper INSTANCE = Mappers.getMapper(GetContactQueryMapper.class);

    @Mapping(source = "contactValue.value", target = "value")
    GetContactQuery toGetContactQuery(Contact contact);
}
