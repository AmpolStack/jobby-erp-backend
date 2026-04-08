package com.jobby.userservice.infrastructure.persistence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Document(collection = "identification_types")
public class MongoIdentificationTypeEntity {
    @Id
    @Field("_id")
    private String id;

    @NotBlank
    @Size(max = 10, message = "It cannot contain more than 100 characters")
    private String name;
}
