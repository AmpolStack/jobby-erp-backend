package com.jobby.userservice.application.mapper;

import com.jobby.userservice.application.queries.GetContactQuery;
import com.jobby.userservice.domain.models.Contact;
import org.mapstruct.Mapper;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface GetContactQueryMapper {

    default GetContactQuery toGetContactQuery(Contact contact) {
        if (contact == null) return null;
        return new GetContactQuery(
                contact.getName(),
                contact.getDescription(),
                contact.isPublic(),
                contact.getValue() != null ? contact.getValue().getValue() : null
        );
    }

    Set<GetContactQuery> toGetContactQuerySet(Set<Contact> contacts);
}
