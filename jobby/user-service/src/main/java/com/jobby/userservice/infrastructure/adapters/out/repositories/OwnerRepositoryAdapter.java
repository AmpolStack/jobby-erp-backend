package com.jobby.userservice.infrastructure.adapters.out.repositories;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.SafeResultValidator;
import com.jobby.userservice.domain.models.Owner;
import com.jobby.userservice.domain.ports.OwnerRepository;
import com.jobby.userservice.infrastructure.mappers.OwnerMapper;
import com.jobby.userservice.infrastructure.persistence.repository.SpringDataMongoOwnersRepository;
import com.jobby.userservice.infrastructure.persistence.repository.SpringDataMongoUsersRepository;
import org.springframework.stereotype.Repository;

@Repository
public class OwnerRepositoryAdapter implements OwnerRepository {

    private final SpringDataMongoOwnersRepository ownersCollection;
    private final SpringDataMongoUsersRepository userCollection;
    private final SafeResultValidator safeResultValidator;
    private final OwnerMapper mapper;

    public OwnerRepositoryAdapter(SpringDataMongoOwnersRepository ownersCollection, SpringDataMongoUsersRepository userCollection, SafeResultValidator safeResultValidator, OwnerMapper mapper) {
        this.ownersCollection = ownersCollection;
        this.userCollection = userCollection;
        this.safeResultValidator = safeResultValidator;
        this.mapper = mapper;
    }

    @Override
    public Result<Owner, Error> create(Owner owner) {
        var ownerMapped = this.mapper.toEntity(owner);

        var ownerResponse = this.mapper.toDomain(ownerMapped);

        return safeResultValidator.validate(ownerMapped)
                .map(v -> ownerResponse);
    }
}
