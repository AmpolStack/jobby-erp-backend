package com.jobby.userservice.infrastructure.transformations;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.infrastructure.security.SecurityOrchestrator;
import com.jobby.domain.ports.transformations.Transformation;
import com.jobby.domain.ports.transformations.TransformationRegistry;
import com.jobby.userservice.domain.models.aggregate.User;
import com.jobby.userservice.infrastructure.persistence.entities.MongoUserEntity;
import com.jobby.userservice.infrastructure.persistence.mappers.entities.MongoUserMapper;
import org.springframework.stereotype.Component;

@Component
public class UserEntityToUserTransformation extends Transformation<MongoUserEntity, User> {

    private final SecurityOrchestrator securityOrchestrator;
    private final MongoUserMapper mapper;

    public UserEntityToUserTransformation(TransformationRegistry registry, SecurityOrchestrator securityOrchestrator, MongoUserMapper mapper) {
        super(registry);
        this.securityOrchestrator = securityOrchestrator;
        this.mapper = mapper;
    }

    @Override
    public Result<User, Error> transform(MongoUserEntity origin) {
        return this.securityOrchestrator.reverse()
                .add(origin.getFirstName())
                .add(origin.getLastName())
                .add(origin.getIdentificationNumber())
                .add(origin.getPhone())
                .add(origin.getEmail())
                .build()
                .map(v -> this.mapper.toDomain(origin));
    }
}
