package com.jobby.userservice.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class IdentificationType {
    private Integer id;
    private int dianCode;
    private String name;
    private int minLength;
    private int maxLength;
    private String expression;
    private String abbreviation;
    private Set<String> allowCharacters;
}
