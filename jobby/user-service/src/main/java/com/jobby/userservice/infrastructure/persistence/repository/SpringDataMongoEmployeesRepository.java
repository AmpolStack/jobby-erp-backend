package com.jobby.userservice.infrastructure.persistence.repository;

import com.jobby.userservice.infrastructure.persistence.entities.MongoEmployeeEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataMongoEmployeesRepository extends MongoRepository<MongoEmployeeEntity, Long> {
}
