package com.jobby.userservice.infrastructure.transformations;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.domain.ports.SafeResultValidator;
import com.jobby.infrastructure.security.SecurityOrchestrator;
import com.jobby.domain.ports.transformations.Transformation;
import com.jobby.domain.ports.transformations.TransformationRegistry;
import com.jobby.userservice.domain.models.aggregate.User;
import com.jobby.userservice.infrastructure.persistence.entities.MongoUserEntity;
import com.jobby.userservice.infrastructure.persistence.mappers.entities.MongoUserMapper;
import org.springframework.stereotype.Component;

@Component
public class UserToUserEntityTransformation extends Transformation<User, MongoUserEntity> {

    private final SecurityOrchestrator securityOrchestrator;
    private final SafeResultValidator validator;
    private final MongoUserMapper mapper;

    public UserToUserEntityTransformation(TransformationRegistry registry,
                                             SecurityOrchestrator securityOrchestrator,
                                             SafeResultValidator validator,
                                             MongoUserMapper mapper) {
        super(registry);
        this.securityOrchestrator = securityOrchestrator;
        this.validator = validator;
        this.mapper = mapper;
    }

    @Override
    public Result<MongoUserEntity, Error> transform(User origin) {
        var entity = this.mapper.toEntity(origin);
        return ValidationChain.create()
                .add(this.securityOrchestrator
                        .secure()
                        .add(entity.getPhone())
                        .add(entity.getEmail())
                        .add(entity.getIdentificationNumber())
                        .add(entity.getLastName())
                        .add(entity.getFirstName())
                        .build())
                .add(this.validator.validate(entity))
                .build()
                .map(v -> entity);
    }
}
