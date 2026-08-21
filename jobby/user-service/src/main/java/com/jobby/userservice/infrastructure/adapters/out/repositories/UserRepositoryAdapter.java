package com.jobby.userservice.infrastructure.adapters.out.repositories;

import com.jobby.domain.functional.PersistenceTask;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.infrastructure.security.SecurityOrchestrator;
import com.jobby.infrastructure.transaction.proxy.PersistenceProxy;
import com.jobby.domain.ports.transformations.TransformationRegistry;
import com.jobby.userservice.domain.models.aggregate.User;
import com.jobby.userservice.domain.ports.out.repositories.UserRepository;
import com.jobby.userservice.infrastructure.persistence.entities.MongoUserEntity;
import com.jobby.userservice.infrastructure.persistence.repository.SpringDataMongoUsersRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final SecurityOrchestrator securityOrchestrator;
    private final PersistenceProxy proxy;
    private final SpringDataMongoUsersRepository userCollection;
    private final TransformationRegistry transformations;

    @Override
    public Result<PersistenceTask, Error> prepareSave(User user) {
        return this.transformations.get(User.class, MongoUserEntity.class)
                .transform(user)
                .map(entity -> () -> this.userCollection.save(entity));
    }

    @Override
    public Result<Boolean, Error> existByEmail(String email) {
        return this.securityOrchestrator.index(email)
                .flatMap(index ->
                        this.proxy.read(
                                () -> this.userCollection.existsByEmail_Index(index)
                        )
                );
    }

    @Override
    public Result<Boolean, Error> existByIdAndEmail(long id, String email) {
        return this.securityOrchestrator.index(email)
                .flatMap(index ->
                        this.proxy.read(
                                () -> this.userCollection.existsByIdAndEmail_Index(id, index)
                        )
                );
    }

    @Override
    public Result<Boolean, Error> existByPhone(String phone) {
        return this.securityOrchestrator.index(phone)
                .flatMap(index ->
                        this.proxy.read(
                            ()-> this.userCollection.existsByPhone_Index(index)
                        )
                );
    }

    @Override
    public Result<Boolean, Error> existByIdentificationNumber(String identificationNumber) {
        return this.securityOrchestrator.index(identificationNumber)
                .flatMap(index ->
                        this.proxy.read(
                            ()-> this.userCollection.existsByIdentificationNumber_Index(index)
                        )
                );
    }

    @Override
    public Result<User, Error> getById(long id) {
        return this.proxy.read(() -> this.userCollection.findById(id))
                .flatMap(optional ->
                        optional.map(entity ->
                                this.transformations.get(MongoUserEntity.class, User.class)
                                        .transform(entity))
                        .orElse(Result.failure(ErrorType.USER_NOT_FOUND,
                                new Field("user", "There is no registered user with that ID")))
                );
    }
}
