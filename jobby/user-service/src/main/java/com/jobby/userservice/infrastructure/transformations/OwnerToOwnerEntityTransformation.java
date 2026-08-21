package com.jobby.userservice.infrastructure.transformations;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.domain.ports.SafeResultValidator;
import com.jobby.infrastructure.security.SecurityOrchestrator;
import com.jobby.domain.ports.transformations.Transformation;
import com.jobby.domain.ports.transformations.TransformationRegistry;
import com.jobby.userservice.domain.models.aggregate.Owner;
import com.jobby.userservice.infrastructure.persistence.entities.MongoOwnerEntity;
import com.jobby.userservice.infrastructure.persistence.mappers.entities.MongoOwnerMapper;
import org.springframework.stereotype.Component;

@Component
public class OwnerToOwnerEntityTransformation extends Transformation<Owner, MongoOwnerEntity> {

    private final MongoOwnerMapper mapper;
    private final SecurityOrchestrator securityOrchestrator;
    private final SafeResultValidator validator;

    public OwnerToOwnerEntityTransformation(TransformationRegistry registry,
                                               MongoOwnerMapper mapper,
                                               SecurityOrchestrator securityOrchestrator,
                                               SafeResultValidator validator) {
        super(registry);
        this.mapper = mapper;
        this.validator = validator;
        this.securityOrchestrator = securityOrchestrator;
    }

    @Override
    public Result<MongoOwnerEntity, Error> transform(Owner origin) {
        var entity = this.mapper.toEntity(origin);
        return ValidationChain.create()
                .add(this.securityOrchestrator
                        .secure()
                        .add(entity.getRecoveryEmail())
                        .build()
                )
                .add(this.validator.validate(entity))
                .build()
                .map(v -> entity);
    }
}
