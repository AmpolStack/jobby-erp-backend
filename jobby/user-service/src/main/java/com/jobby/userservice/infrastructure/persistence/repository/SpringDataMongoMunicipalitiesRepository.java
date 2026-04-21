package com.jobby.userservice.infrastructure.persistence.repository;

import com.jobby.userservice.infrastructure.persistence.entities.MongoMunicipalityEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataMongoMunicipalitiesRepository extends MongoRepository<MongoMunicipalityEntity, Integer> {
}
