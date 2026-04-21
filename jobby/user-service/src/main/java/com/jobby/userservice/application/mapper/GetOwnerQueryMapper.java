package com.jobby.userservice.application.mapper;

import com.jobby.userservice.application.queries.GetOwnerQuery;
import com.jobby.userservice.domain.models.Owner;
import com.jobby.userservice.domain.models.User;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {GetUserQueryMapper.class})
public abstract class GetOwnerQueryMapper {

    @Autowired
    protected GetUserQueryMapper getUserQueryMapper;

    public GetOwnerQuery toGetOwnerQuery(Owner owner, User user) {
        if (owner == null) return null;
        return new GetOwnerQuery(
                this.getUserQueryMapper.toGetUserQuery(user),
                owner.getAlternativeEmail() != null ? owner.getAlternativeEmail().getEmail() : null
        );
    }
}
