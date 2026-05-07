package com.jobby.userservice.infrastructure.persistence.repository;

import com.jobby.userservice.infrastructure.persistence.entities.MongoUserEntity;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SpringDataMongoUsersRepository extends MongoRepository<MongoUserEntity, Long> {

    @Query("{ $or: [{'email.index': ?0}, {'phone.index': ?1}, {'identification_number.index': ?2}] }")
    List<MongoUserEntity> findByAnyIndex(@NotNull byte[] emailIndex,
                                         @NotNull byte[] phoneIndex,
                                         @NotNull byte[] identificationNumberIndex);

    Boolean existsByEmail_Index(@NotNull byte[] emailIndex);

    Boolean existsByPhone_Index(@NotNull byte[] phoneIndex);

    Boolean existsByIdentificationNumber_Index(@NotNull byte[] identificationNumberIndex);

    <T> T existsByIdAndEmail_Index(Long id, @NotNull byte[] emailIndex);
}

