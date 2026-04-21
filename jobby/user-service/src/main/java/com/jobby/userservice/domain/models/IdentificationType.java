package com.jobby.userservice.domain.models;

import lombok.*;
import java.util.Set;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IdentificationType {
    private Integer id;
    private int dianCode;
    private String name;
    private int minLength;
    private int maxLength;
    private String expression;
    private String abbreviation;
    private Set<String> allowCharacters;

    public static IdentificationType reconstruct(int id,
                                                 int dianCode,
                                                 String name,
                                                 int minLength,
                                                 int maxLength,
                                                 String expression,
                                                 String abbreviation,
                                                 Set<String> allowCharacters){
        return new IdentificationType(id,
                dianCode,
                name,
                minLength,
                maxLength,
                expression,
                abbreviation,
                allowCharacters);
    }

}
