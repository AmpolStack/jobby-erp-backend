package com.jobby.userservice.domain.models;

import com.jobby.userservice.domain.vo.Email;
import com.jobby.userservice.domain.vo.IdentificationNumber;
import com.jobby.userservice.domain.vo.ImageUrl;
import com.jobby.userservice.domain.vo.Phone;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
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
