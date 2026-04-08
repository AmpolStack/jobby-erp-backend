package com.jobby.userservice.infrastructure.persistence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.Instant;
import java.util.Map;

@Setter
@Getter
@Document("owners")
public class MongoOwnerEntity {
    @Id
    @Field("_id")
    private String id;

    @NotNull
    private MongoUserEntity user;

    @Field("alternative_email")
    private byte[] alternativeEmail;

    @Field("alternative_email_searchable")
    private byte[] alternativeEmailSearchable;


    private Map<
            @NotBlank @Size(max = 20, message = "It cannot have more than 20 characters") String,
            @NotBlank @Size(max = 100, message = "It cannot have more than 100 characters")String>
            secureParameters;

    @NotNull
    @Field("created_at")
    private Instant createdAt;

    @NotNull
    @Field("modified_at")
    private Instant modifiedAt;
}
