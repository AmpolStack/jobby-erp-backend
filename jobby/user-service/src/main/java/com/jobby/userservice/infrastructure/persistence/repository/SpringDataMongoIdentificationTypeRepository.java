package com.jobby.userservice.infrastructure.persistence.repository;

import com.jobby.userservice.infrastructure.persistence.entities.MongoIdentificationTypeEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataMongoIdentificationTypeRepository extends MongoRepository<MongoIdentificationTypeEntity,Integer> {
}
