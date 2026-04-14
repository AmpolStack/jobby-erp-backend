package com.jobby.userservice.infrastructure.adapters.out.repositories;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.SafeResultValidator;
import com.jobby.infrastructure.transaction.TransactionalContext;
import com.jobby.userservice.domain.models.Owner;
import com.jobby.userservice.domain.ports.OwnerRepository;
import com.jobby.userservice.infrastructure.mappers.OwnerMapper;
import com.jobby.userservice.infrastructure.persistence.repository.SpringDataMongoOwnersRepository;
import com.jobby.userservice.infrastructure.persistence.repository.SpringDataMongoUsersRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class OwnerRepositoryAdapter implements OwnerRepository {

    private final SpringDataMongoOwnersRepository ownersCollection;
    private final SpringDataMongoUsersRepository userCollection;
    private final SafeResultValidator safeResultValidator;
    private final OwnerMapper mapper;
    private final TransactionalContext context;

    @Override
    public Result<Owner, Error> create(Owner owner) {
        var ownerMapped = this.mapper.toEntity(owner);

        return safeResultValidator.validate(ownerMapped)
                .flatMap(v -> this.context.run(() ->{
                        this.ownersCollection.save(ownerMapped);
                        this.userCollection.save(ownerMapped.getUser());
                        return Result.success(null);
                }))
                .map(v -> this.mapper.toDomain(ownerMapped));
    }
}
