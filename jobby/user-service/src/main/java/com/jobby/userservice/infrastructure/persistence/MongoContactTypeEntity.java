package com.jobby.userservice.infrastructure.persistence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

@Setter
@Getter
@Document(collection = "contact_types")
public class MongoContactTypeEntity {
    @Id
    @Field("_id")
    private Integer id;

    @NotBlank
    @Size(max = 50, message = "The number of characters must be less than or equal to 50")
    private String type;

    @NotBlank
    @Size(max = 150, message = "It cannot contain more than 150 characters")
    private String description;

    @Field("visual_instructions")
    private Map<
            @NotBlank @Size(max = 20, message = "It cannot have more than 20 chars") String,
            @NotBlank @Size(max = 100, message = "It cannot have more than 100 chars") String>
            visualInstructions;
}
