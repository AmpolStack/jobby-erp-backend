package com.jobby.userservice.infrastructure.persistence.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Document(collection = "municipalities")
public class MongoMunicipalityEntity {
    @Id
    @Field("_id")
    private Integer id;

    @NotNull
    private MongoDepartmentEntity department;

    @NotBlank
    @Size(max = 50, message = "It cannot have more than 50 characters")
    private String name;

    @Field("dane_code")
    private int daneCode;
}
