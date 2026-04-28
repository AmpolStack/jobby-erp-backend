package com.jobby.userservice.infrastructure.adapters.out.repositories;

import com.jobby.domain.functional.PersistenceTask;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.domain.ports.SafeResultValidator;
import com.jobby.domain.ports.TransactionOrchestrator;
import com.jobby.userservice.domain.models.Owner;
import com.jobby.userservice.domain.ports.OwnerRepository;
import com.jobby.userservice.infrastructure.mappers.entities.MongoOwnerMapper;
import com.jobby.userservice.infrastructure.persistence.repository.SpringDataMongoOwnersRepository;
import com.jobby.userservice.infrastructure.persistence.repository.SpringDataMongoUsersRepository;
import com.jobby.infrastructure.transaction.proxy.MongoDbSpringDataHandler;
import com.jobby.infrastructure.security.SecurityOrchestrator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class OwnerRepositoryAdapter implements OwnerRepository {

    private final SpringDataMongoOwnersRepository ownersCollection;
    private final SafeResultValidator safeResultValidator;
    private final MongoOwnerMapper mongoOwnerMapper;
    private final SecurityOrchestrator securityOrchestrator;

    @Override
    public Result<PersistenceTask, Error> prepareSave(Owner owner) {
        var ownerEntity = this.mongoOwnerMapper.toEntity(owner);
        return ValidationChain.create()
                .add(this.securityOrchestrator
                        .secure()
                        .add(ownerEntity.getAlternativeEmail())
                        .build()
                )
                .add(this.safeResultValidator.validate(ownerEntity))
                .build()
                .map(v -> () -> this.ownersCollection.save(ownerEntity));
    }
}
