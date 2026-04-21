package com.jobby.userservice.infrastructure.persistence.entities;

import com.jobby.infrastructure.security.fields.ProtectedField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private Long id;

    @Field("contact_type_id")
    private int contactTypeId;

    @NotBlank
    @Size(max = 50, message = "It cannot contain more than 50 characters")
    private String name;

    @Size(max = 150, message = "It cannot contain more than 150 characters")
    private String description;

    @Field("is_public")
    private boolean isPublic;

    @NotNull
    private ProtectedField value;
}
