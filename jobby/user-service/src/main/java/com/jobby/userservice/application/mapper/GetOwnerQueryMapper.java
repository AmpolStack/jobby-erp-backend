package com.jobby.userservice.application.mapper;

import com.jobby.userservice.application.responses.GetOwnerQuery;
import com.jobby.userservice.domain.models.Owner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {GetUserQueryMapper.class})
public interface GetOwnerQueryMapper {

    GetOwnerQueryMapper INSTANCE = Mappers.getMapper(GetOwnerQueryMapper.class);

    @Mapping(source = "alternativeEmail.email", target = "alternativeEmail")
    GetOwnerQuery toGetOwnerQuery(Owner owner);
}
