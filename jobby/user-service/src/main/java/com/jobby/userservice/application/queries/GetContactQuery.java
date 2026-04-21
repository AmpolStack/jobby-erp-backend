package com.jobby.userservice.application.queries;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetContactQuery {
    private String name;
    private String description;
    private boolean isPublic;
    private String value;
}
