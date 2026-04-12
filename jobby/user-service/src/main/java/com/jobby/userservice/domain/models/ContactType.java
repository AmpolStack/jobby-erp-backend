package com.jobby.userservice.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ContactType {
    private Integer id;
    private String type;
    private String description;
    private String expression;
    private Map<String, String> visualInstructions;
}
