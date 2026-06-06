package com.jobby.userservice.application.mappers;

import com.jobby.userservice.domain.contract.responses.ContactResponse;
import com.jobby.userservice.domain.contract.responses.OwnerResponse;
import com.jobby.userservice.domain.contract.responses.UserResponse;
import com.jobby.userservice.domain.models.entity.Contact;
import com.jobby.userservice.domain.models.aggregate.Owner;
import com.jobby.userservice.domain.models.aggregate.User;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class ResponseMapper {

    public ContactResponse toResponse(Contact contact) {
        if (contact == null) return null;
        return new ContactResponse(
                contact.getName(),
                contact.getDescription(),
                contact.isPublic(),
                contact.getValue() != null ? contact.getValue().getValue() : null
        );
    }

    public abstract Set<ContactResponse> toResponseSet(Set<Contact> contacts);

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return new UserResponse(
                this.toResponseSet(user.getContacts()),
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

    public OwnerResponse toResponse(Owner owner, User user) {
        if (owner == null) return null;
        return new OwnerResponse(
                this.toResponse(user),
                owner.getRecoveryEmail() != null ? owner.getRecoveryEmail().getEmail() : null
        );
    }
}
