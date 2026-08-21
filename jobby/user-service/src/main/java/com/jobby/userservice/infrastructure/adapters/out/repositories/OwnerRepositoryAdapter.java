package com.jobby.userservice.infrastructure.adapters.out.repositories;

import com.jobby.domain.functional.PersistenceTask;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.infrastructure.transaction.proxy.PersistenceProxy;
import com.jobby.domain.ports.transformations.TransformationRegistry;
import com.jobby.userservice.domain.models.aggregate.Owner;
import com.jobby.userservice.domain.ports.out.repositories.OwnerRepository;
import com.jobby.userservice.infrastructure.persistence.entities.MongoOwnerEntity;
import com.jobby.userservice.infrastructure.persistence.repository.SpringDataMongoOwnersRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class OwnerRepositoryAdapter implements OwnerRepository {

    private final SpringDataMongoOwnersRepository ownersCollection;
    private final PersistenceProxy proxy;
    private final TransformationRegistry transformations;

    @Override
    public Result<PersistenceTask, Error> prepareSave(Owner owner) {
        return this.transformations.get(Owner.class, MongoOwnerEntity.class)
                .transform(owner)
                .map(entity -> () -> this.ownersCollection.save(entity));
    }

    @Override
    public Result<Owner, Error> getById(long id) {
        return this.proxy.read(()-> this.ownersCollection.findById(id))
                .flatMap(optional
                        -> optional.map(owner ->
                                this.transformations.get(MongoOwnerEntity.class, Owner.class)
                                        .transform(owner))
                        .orElse(Result.failure(ErrorType.USER_NOT_FOUND,
                                new Field("owner", "There is no registered owner with that ID")))
                );
    }

    @Override
    public Result<Owner, Error> getByUserId(long userId) {
        return this.proxy.read(()-> this.ownersCollection.findByUserId(userId))
                .flatMap(optional
                        -> optional.map(owner ->
                                this.transformations.get(MongoOwnerEntity.class, Owner.class)
                                        .transform(owner))
                        .orElse(Result.failure(ErrorType.USER_NOT_FOUND,
                                new Field("owner", "There is no registered owner with that User ID")))
                );
    }
}

