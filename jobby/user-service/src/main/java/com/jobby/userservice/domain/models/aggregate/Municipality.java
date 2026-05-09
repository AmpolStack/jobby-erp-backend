package com.jobby.userservice.domain.models;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Municipality {
    private Integer id;
    private Department department;
    private String name;
    private int daneCode;

    public static Municipality reconstruct(int id,
                                           Department department,
                                           String name,
                                           int daneCode){
        return new Municipality(id,
                department,
                name,
                daneCode);
    }
}
