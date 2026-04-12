package com.jobby.userservice.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Municipality {
    private Integer id;
    private Department department;
    private String name;
    private int daneCode;
}
