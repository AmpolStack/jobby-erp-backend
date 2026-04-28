package com.jobby.userservice.infrastructure.adapters.out.repositories;

import com.jobby.domain.functional.PersistenceTask;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.infrastructure.security.SecurityOrchestrator;
import com.jobby.infrastructure.transaction.proxy.PersistenceProxy;
import com.jobby.userservice.domain.models.User;
import com.jobby.userservice.domain.ports.UserRepository;
import com.jobby.userservice.infrastructure.mappers.entities.MongoUserMapper;
import com.jobby.userservice.infrastructure.persistence.repository.SpringDataMongoUsersRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final SecurityOrchestrator securityOrchestrator;
    private final PersistenceProxy persistenceProxy;
    private final SpringDataMongoUsersRepository userCollection;
    private final MongoUserMapper mapper;

    @Override
    public Result<PersistenceTask, Error> prepareSave(User user) {
        var mapped = this.mapper.toEntity(user);
        return this.securityOrchestrator
                .secure()
                .add(mapped.getPhone())
                .add(mapped.getEmail())
                .add(mapped.getIdentificationNumber())
                .add(mapped.getLastName())
                .add(mapped.getFirstName())
                .build()
                .map(v -> () -> this.userCollection.save(mapped));
    }

    @Override
    public Result<Boolean, Error> existByEmail(String email) {
        return this.securityOrchestrator.index(email)
                .flatMap(index ->
                        this.persistenceProxy.read(
                                () -> this.userCollection.existsByEmail_Index(index)
                        )
                );
    }

    @Override
    public Result<Boolean, Error> existByPhone(String phone) {
        return this.securityOrchestrator.index(phone)
                .flatMap(index ->
                        this.persistenceProxy.read(
                            ()-> this.userCollection.existsByPhone_Index(index)
                        )
                );
    }

    @Override
    public Result<Boolean, Error> existByIdentificationNumber(String identificationNumber) {
        return this.securityOrchestrator.index(identificationNumber)
                .flatMap(index ->
                        this.persistenceProxy.read(
                            ()-> this.userCollection.existsByIdentificationNumber_Index(index)
                        )
                );
    }
}
