package com.jobby.userservice.infrastructure.transformations;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.infrastructure.security.SecurityOrchestrator;
import com.jobby.domain.ports.transformations.Transformation;
import com.jobby.domain.ports.transformations.TransformationRegistry;
import com.jobby.userservice.domain.models.aggregate.Owner;
import com.jobby.userservice.infrastructure.persistence.entities.MongoOwnerEntity;
import com.jobby.userservice.infrastructure.persistence.mappers.entities.MongoOwnerMapper;
import org.springframework.stereotype.Component;

@Component
public class OwnerEntityToOwnerTransformation extends Transformation<MongoOwnerEntity, Owner> {

    private final MongoOwnerMapper mapper;
    private final SecurityOrchestrator securityOrchestrator;

    public OwnerEntityToOwnerTransformation(TransformationRegistry registry,
                                             MongoOwnerMapper mapper,
                                             SecurityOrchestrator securityOrchestrator) {
        super(registry);
        this.mapper = mapper;
        this.securityOrchestrator = securityOrchestrator;
    }

    @Override
    public Result<Owner, Error> transform(MongoOwnerEntity origin) {
        return this.securityOrchestrator
                .reverse()
                .add(origin.getRecoveryEmail())
                .build()
                .map(v -> this.mapper.toDomain(origin));
    }
}
