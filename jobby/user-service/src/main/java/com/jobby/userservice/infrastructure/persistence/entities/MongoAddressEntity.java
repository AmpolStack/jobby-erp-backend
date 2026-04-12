package com.jobby.userservice.infrastructure.persistence.entities;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Getter
@Setter
public class MongoAddressEntity {
    @Id
    @Field("_id")
    private Long id;

    @NotNull
    private MongoMunicipalityEntity municipality;

    @NotNull
    private byte[] direction;

    @NotNull
    @Field("created_at")
    private Instant createdAt;

    @NotNull
    @Field("created_at")
    private Instant modifiedAt;
}
