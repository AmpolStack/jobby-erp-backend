package com.jobby.userservice.domain.models;

import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Department {
    private Integer id;
    private String name;
    private int daneCode;

    public static Department reconstruct(int id, String name, int daneCode){
        return new Department(id, name, daneCode);
    }
}
