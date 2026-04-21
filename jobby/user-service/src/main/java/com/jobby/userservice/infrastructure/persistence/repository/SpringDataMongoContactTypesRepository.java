package com.jobby.userservice.infrastructure.persistence.repository;

import com.jobby.userservice.infrastructure.persistence.entities.MongoContactTypeEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataMongoContactTypesRepository extends MongoRepository<MongoContactTypeEntity, Integer> {
}
