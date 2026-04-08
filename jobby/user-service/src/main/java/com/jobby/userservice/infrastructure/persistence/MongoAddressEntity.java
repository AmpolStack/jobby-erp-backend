package com.jobby.userservice.infrastructure.persistence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Getter
@Setter
public class MongoAddressEntity {
    @Id
    @Field("_id")
    private String id;

    @NotNull
    private MongoMunicipalityEntity municipality;

    @NotBlank
    @Size(max = 200, message = "It cannot have more than 200 characters")
    private String direction;

    @NotNull
    @Field("created_at")
    private Instant createdAt;

    @NotNull
    @Field("created_at")
    private Instant modifiedAt;
}
