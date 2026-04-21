package com.jobby.userservice.infrastructure.adapters.out.repositories;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.domain.ports.SafeResultValidator;
import com.jobby.userservice.domain.models.Owner;
import com.jobby.userservice.domain.models.User;
import com.jobby.userservice.domain.ports.OwnerRepository;
import com.jobby.userservice.infrastructure.mappers.entities.MongoOwnerMapper;
import com.jobby.userservice.infrastructure.mappers.entities.MongoUserMapper;
import com.jobby.userservice.infrastructure.persistence.repository.SpringDataMongoOwnersRepository;
import com.jobby.userservice.infrastructure.persistence.repository.SpringDataMongoUsersRepository;
import com.jobby.infrastructure.transaction.proxy.MongoDbSpringDataHandler;
import com.jobby.infrastructure.security.SecurityOrchestrator;
import com.jobby.infrastructure.transaction.TransactionExecutor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class OwnerRepositoryAdapter implements OwnerRepository {

    private final SpringDataMongoOwnersRepository ownersCollection;
    private final SpringDataMongoUsersRepository userCollection;
    private final SafeResultValidator safeResultValidator;
    private final MongoOwnerMapper mongoOwnerMapper;
    private final MongoUserMapper userMapper;
    private final TransactionExecutor transaction;
    private final SecurityOrchestrator securityOrchestrator;

    @Override
    public Result<Void, Error> save(Owner owner, User user) {
        var ownerEntity = this.mongoOwnerMapper.toEntity(owner);
        var userEntity = this.userMapper.toEntity(user);

        return ValidationChain.create()
                .add(this.securityOrchestrator
                        .secure()
                        .add(ownerEntity.getAlternativeEmail())
                        .add(userEntity.getEmail())
                        .add(userEntity.getPhone())
                        .add(userEntity.getFirstName())
                        .add(userEntity.getLastName())
                        .add(userEntity.getIdentificationNumber())
                        .build()
                )
                .add(this.safeResultValidator.validate(ownerEntity))
                .add(this.safeResultValidator.validate(userEntity))
                .build()
                .flatMap(v -> this.transaction
                        .write()
                        .add(()-> this.ownersCollection.save(ownerEntity),
                                new MongoDbSpringDataHandler())
                        .add(()-> this.userCollection.save(userEntity),
                                new MongoDbSpringDataHandler())
                        .build()
                );
    }

    @Override
    public Result<Boolean, Error> existByEmail(String email) {
        return this.securityOrchestrator.index(email)
                .flatMap(index -> this.transaction.read(
                        ()-> this.userCollection.existsByEmail_Index(index),
                        new MongoDbSpringDataHandler())
                );
    }

    @Override
    public Result<Boolean, Error> existByPhone(String phone) {
        return this.securityOrchestrator.index(phone)
                .flatMap(index -> this.transaction.read(
                        ()-> this.userCollection.existsByPhone_Index(index),
                        new MongoDbSpringDataHandler())
                );
    }

    @Override
    public Result<Boolean, Error> existByIdentificationNumber(String identificationNumber) {
        return this.securityOrchestrator.index(identificationNumber)
                .flatMap(index -> this.transaction.read(
                        ()-> this.userCollection.existsByIdentificationNumber_Index(index),
                        new MongoDbSpringDataHandler())
                );
    }
}
