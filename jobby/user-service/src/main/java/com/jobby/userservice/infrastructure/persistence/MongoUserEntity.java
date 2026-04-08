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
import java.util.Set;

@Setter
@Getter
@Document(collection = "users")
public class MongoUserEntity {
    @Id
    @Field("_id")
    private String id;

    private Set<@NotNull MongoContactEntity> contacts;

    @NotNull
    @Field("identification_type")
    private MongoIdentificationTypeEntity identificationType;

    @Field("first_name")
    @NotBlank
    @Size(max = 150, message = "It cannot have more than 150 characters")
    private String firstName;

    @Field("last_name")
    @Size(max = 150, message = "It cannot have more than 150 characters")
    private String lastName;

    @NotBlank
    @Size(max = 10, message = "It cannot have more than 10 characters")
    private String role;

    @Field("is_active")
    private boolean isActive;

    @Field("profile_image_url")
    private String profileImageUrl;

    @Field("identification_number")
    @NotNull
    private byte[] identificationNumber;

    @NotNull
    @Field("identification_number_searchable")
    private byte[] identificationNumberSearchable;

    @NotNull
    private byte[] email;

    @NotNull
    @Field("email_searchable")
    private byte[] emailSearchable;

    @NotNull
    private String phone;

    @NotNull
    @Field("phone_searchable")
    private String phoneSearchable;

    @NotNull
    @Field("created_at")
    private Instant createdAt;

    @NotNull
    @Field("modified_at")
    private Instant modifiedAt;
}
