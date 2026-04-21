package com.jobby.userservice.domain.models;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Map;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContactType {
    private Integer id;
    private String type;
    private String description;
    private String expression;
    private Map<String, String> visualInstructions;

    public static ContactType reconstruct(int id,
                                          String type,
                                          String description,
                                          String expression,
                                          Map<String, String> visualInstructions){
        return new ContactType(id,type,description, expression, visualInstructions);
    }
}
