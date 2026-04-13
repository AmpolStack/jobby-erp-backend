package com.jobby.userservice.application.responses;

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
public class GetUserQuery {
    private Set<GetContactQuery> contacts;
    private int identificationTypeId;
    private String firstName;
    private String lastName;
    private String role;
    private boolean isActive;
    private String profileImageUrl;
    private String identificationNumber;
    private String email;
    private String phone;
    private Instant createdAt;
    private Instant modifiedAt;
}
