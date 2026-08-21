package com.jobby.userservice.infrastructure.persistence.entities;

import com.jobby.infrastructure.security.fields.IndexedField;
import com.jobby.infrastructure.security.fields.ProtectedField;
import com.jobby.userservice.domain.models.enums.Role;
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
    private Long id;

    @Field("contacts")
    private Set<@NotNull MongoContactEntity> contacts;

    @Field("identification_type_id")
    private int identificationTypeId;

    @Field("first_name")
    @NotNull
    private ProtectedField firstName;

    @Field("last_name")
    @NotNull
    private ProtectedField lastName;

    @Field("role")
    @NotNull
    private Role role;

    @Field("is_active")
    private boolean isActive;

    @Field("profile_image_url")
    private String profileImageUrl;

    @Field("identification_number")
    @NotNull
    private IndexedField identificationNumber;

    @Field("email")
    @NotNull
    private IndexedField email;

    @Field("phone")
    @NotNull
    private IndexedField phone;

    @NotNull
    @Field("created_at")
    private Instant createdAt;

    @NotNull
    @Field("modified_at")
    private Instant modifiedAt;
}
