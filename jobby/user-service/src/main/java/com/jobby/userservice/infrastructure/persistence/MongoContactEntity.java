package com.jobby.userservice.infrastructure.persistence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
public class MongoContactEntity {
    @Id
    @Field("_id")
    private String id;

    @NotBlank
    @Size(max = 50, message = "It cannot contain more than 50 characters")
    private String name;

    @Size(max = 150, message = "It cannot contain more than 150 characters")
    private String description;

    @Field("is_public")
    private boolean isPublic;

    @NotBlank
    @Size(max=250, message = "It cannot contain more than 250 characters")
    private String value;
}
