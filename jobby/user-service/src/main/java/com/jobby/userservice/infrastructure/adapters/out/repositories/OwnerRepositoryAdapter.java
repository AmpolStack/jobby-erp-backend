package com.jobby.userservice.infrastructure.adapters.out.repositories;

import com.jobby.domain.functional.PersistenceTask;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.domain.ports.SafeResultValidator;
import com.jobby.infrastructure.security.SecurityOrchestrator;
import com.jobby.infrastructure.transaction.proxy.PersistenceProxy;
import com.jobby.userservice.domain.models.Owner;
import com.jobby.userservice.domain.ports.out.repositories.models.OwnerRepository;
import com.jobby.userservice.infrastructure.persistence.mappers.entities.MongoOwnerMapper;
import com.jobby.userservice.infrastructure.persistence.repository.SpringDataMongoOwnersRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class OwnerRepositoryAdapter implements OwnerRepository {

    private final SpringDataMongoOwnersRepository ownersCollection;
    private final SafeResultValidator safeResultValidator;
    private final MongoOwnerMapper mongoOwnerMapper;
    private final SecurityOrchestrator securityOrchestrator;
    private final PersistenceProxy proxy;

    @Override
    public Result<PersistenceTask, Error> prepareSave(Owner owner) {
        var ownerEntity = this.mongoOwnerMapper.toEntity(owner);
        return ValidationChain.create()
                .add(this.securityOrchestrator
                        .secure()
                        .add(ownerEntity.getRecoveryEmail())
                        .build()
                )
                .add(this.safeResultValidator.validate(ownerEntity))
                .build()
                .map(v -> () -> this.ownersCollection.save(ownerEntity));
    }

    @Override
    public Result<Owner, Error> getById(long id) {
        return this.proxy.read(()-> this.ownersCollection.findById(id))
                .flatMap(optional
                        -> optional.map(owner -> this.securityOrchestrator
                                .reverse()
                                .add(owner.getRecoveryEmail())
                                .build()
                                .map(v -> this.mongoOwnerMapper.toDomain(owner)))
                        .orElse(Result.failure(ErrorType.USER_NOT_FOUND,
                                new Field("owner", "There is no registered owner with that ID")))
                );
    }

    @Override
    public Result<Owner, Error> getByUserId(long userId) {
        return this.proxy.read(()-> this.ownersCollection.findByUserId(userId))
                .flatMap(optional
                        -> optional.map(owner -> this.securityOrchestrator
                                .reverse()
                                .add(owner.getRecoveryEmail())
                                .build()
                                .map(v -> this.mongoOwnerMapper.toDomain(owner)))
                        .orElse(Result.failure(ErrorType.USER_NOT_FOUND,
                                new Field("owner", "There is no registered owner with that ID")))
                );
    }
}

