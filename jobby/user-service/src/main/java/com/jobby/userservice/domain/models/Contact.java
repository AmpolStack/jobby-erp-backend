package com.jobby.userservice.domain.models;

import com.jobby.userservice.domain.vo.ContactValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Contact {
    private Long id;
    private String name;
    private String description;
    private boolean isPublic;
    private ContactValue contactValue;
}
