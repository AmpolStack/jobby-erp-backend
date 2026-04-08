package com.jobby.userservice.infrastructure.persistence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
public class MongoDepartmentEntity {
    @Id
    @Field("_id")
    private String id;

    @NotBlank
    @Size(max = 50, message = "It cannot contain more than 50 characters")
    private String name;

    @Field("dane_code")
    private int daneCode;
}
