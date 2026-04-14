package com.jobby.userservice.infrastructure.persistence.entities;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Document(collection = "identification_types")
public class MongoIdentificationTypeEntity {
    @Id
    @Field("_id")
    private Integer id;

    @Min(10)
    @Max(99)
    @Field("dian_code")
    private int dianCode;

    @Min(value = 1, message = "It cannot be less than 1")
    @Field("min_length")
    private int minLength;

    @Min(value = 1, message = "It cannot be less than 1")
    @Max(value = 50, message = "It cannot be greater than 50")
    @Field("max_length")
    private int maxLength;

    @NotBlank
    @Size(max = 50, message = "It cannot contain more than 100 characters")
    private String name;

    @NotBlank
    @Size(max=200, message = "It cannot contain more than 200 characters")
    private String expression;

    @NotBlank
    @Size(max=5, message = "It cannot contain more than 100 characters")
    private String abbreviation;

    @NotNull
    @Field("allow_characters")
    private Set<@Size(max=15,message = "It cannot contain more than 15 characters") String> allowCharacters;

}
