package com.jobby.userservice.infrastructure.persistence.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document("employees")
@Getter
@Setter
public class MongoEmployeeEntity {
    @Id
    @Field("_id")
    private Long id;

    private MongoAddressEntity address;

    @Field("sectional_id")
    private int sectionalId;

    @Field("position_name")
    @NotBlank
    @Size(max = 100, message = "It cannot have more than 100 characters")
    private String positionName;

    @NotNull
    @Field("created_at")
    private Instant createdAt;

    @NotNull
    @Field("modified_at")
    private Instant modifiedAt;
}
