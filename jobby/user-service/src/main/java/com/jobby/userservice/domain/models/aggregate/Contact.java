package com.jobby.userservice.domain.models;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.userservice.domain.vo.ContactValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Contact {
    private Long id;
    private int contactTypeId;
    private String name;
    private String description;
    private boolean isPublic;
    private ContactValue value;

    public static Result<Contact, Error> create(long id,
                                                int contactTypeId,
                                                String name,
                                                String description,
                                                boolean isPublic,
                                                ContactValue contact){
        return ValidationChain.create()
                .validateNotBlank(name, "contact name")
                .validateNotNull(contact, "contact value")
                .build()
                .map(v -> new Contact(id,
                        contactTypeId,
                        name,
                        description,
                        isPublic,
                        contact));
    }

    public static Contact reconstruct(long id,
                                      int contactTypeId,
                                      String name,
                                      String description,
                                      boolean isPublic,
                                      ContactValue value){
        return new Contact(id,
                contactTypeId,
                name,
                description,
                isPublic,
                value);
    }
}

