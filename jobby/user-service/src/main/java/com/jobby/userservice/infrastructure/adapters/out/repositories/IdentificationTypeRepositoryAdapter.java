package com.jobby.userservice.infrastructure.adapters.out.repositories;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.infrastructure.transaction.proxy.PersistenceProxy;
import com.jobby.userservice.domain.models.IdentificationType;
import com.jobby.userservice.domain.ports.IdentificationTypeRepository;
import com.jobby.userservice.infrastructure.mappers.entities.MongoIdentificationTypeMapper;
import com.jobby.userservice.infrastructure.persistence.repository.SpringDataMongoIdentificationTypeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class IdentificationTypeRepositoryAdapter implements IdentificationTypeRepository {

    private final SpringDataMongoIdentificationTypeRepository repository;
    private final MongoIdentificationTypeMapper mapper;
    private final PersistenceProxy persistenceProxy;

    @Override
    public Result<IdentificationType, Error> findById(int id) {
        return persistenceProxy.read(() -> this.repository.findById(id))
                .flatMap(maybeType -> maybeType
                        .map(this.mapper::toDomain)
                        .map(Result::<IdentificationType, Error>success)
                        .orElse(Result.failure(ErrorType.NOT_FOUND,
                                new Field("identification type", "There is no registered identification type with that ID")))
                );
    }

    @Override
    public Result<List<IdentificationType>, Error> getAll() {
        return persistenceProxy.read(() -> {
            var response = this.repository.findAll();
            return this.mapper.toDomain(response);
        });
    }
}
