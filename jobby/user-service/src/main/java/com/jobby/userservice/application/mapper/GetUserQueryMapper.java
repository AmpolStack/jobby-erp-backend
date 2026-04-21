package com.jobby.userservice.application.mapper;

import com.jobby.userservice.application.queries.GetUserQuery;
import com.jobby.userservice.domain.models.User;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {GetContactQueryMapper.class})
public abstract class GetUserQueryMapper {

    @Autowired
    protected GetContactQueryMapper getContactQueryMapper;

    public GetUserQuery toGetUserQuery(User user) {
        if (user == null) return null;
        return new GetUserQuery(
                this.getContactQueryMapper.toGetContactQuerySet(user.getContacts()),
                user.getIdentificationTypeId(),
                user.getFirstName() != null ? user.getFirstName().getValue() : null,
                user.getLastName() != null ? user.getLastName().getValue() : null,
                user.getRole(),
                user.isActive(),
                user.getProfileImageUrl() != null ? user.getProfileImageUrl().getValue() : null,
                user.getIdentificationNumber() != null ? user.getIdentificationNumber().getNumber() : null,
                user.getEmail() != null ? user.getEmail().getEmail() : null,
                user.getPhone() != null ? user.getPhone().getNumber() : null,
                user.getCreatedAt(),
                user.getModifiedAt()
        );
    }
}
