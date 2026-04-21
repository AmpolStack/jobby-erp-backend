package com.jobby.userservice.infrastructure.persistence.repository;

import com.jobby.userservice.infrastructure.persistence.entities.MongoUserEntity;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SpringDataMongoUsersRepository extends MongoRepository<MongoUserEntity, Long> {
    List<MongoUserEntity> findByEmail_IndexOrPhone_IndexOrIdentificationNumber_Index(@NotNull byte[] emailIndex, @NotNull byte[] phoneIndex, @NotNull byte[] identificationNumberIndex);

    Boolean existsByEmail_Index(@NotNull byte[] emailIndex);

    Boolean existsByPhone_Index(@NotNull byte[] phoneIndex);

    Boolean existsByIdentificationNumber_Index(@NotNull byte[] identificationNumberIndex);
}
