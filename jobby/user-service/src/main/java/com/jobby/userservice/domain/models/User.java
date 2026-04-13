package com.jobby.userservice.domain.models;

import com.jobby.userservice.domain.vo.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.Set;

@Setter
@Getter
public class User {
    private Long id;
    private Set<Contact> contacts;
    private int identificationTypeId;
    private String firstName;
    private String lastName;
    private String role;
    private boolean isActive;
    private ImageUrl profileImageUrl;
    private IdentificationNumber identificationNumber;
    private Email email;
    private Phone phone;
    private Instant createdAt;
    private Instant modifiedAt;
}
