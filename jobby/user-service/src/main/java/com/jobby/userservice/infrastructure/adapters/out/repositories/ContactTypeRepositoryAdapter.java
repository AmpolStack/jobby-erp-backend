package com.jobby.userservice.infrastructure.adapters.out.repositories;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.infrastructure.transaction.proxy.PersistenceProxy;
import com.jobby.userservice.domain.models.ContactType;
import com.jobby.userservice.domain.ports.ContactTypeRepository;
import com.jobby.userservice.infrastructure.mappers.entities.MongoContactTypeMapper;
import com.jobby.userservice.infrastructure.persistence.repository.SpringDataMongoContactTypesRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class ContactTypeRepositoryAdapter implements ContactTypeRepository {

    private final SpringDataMongoContactTypesRepository repository;
    private final MongoContactTypeMapper mapper;
    private final PersistenceProxy persistenceProxy;

    @Override
    public Result<List<ContactType>, Error> findAll() {
        return persistenceProxy.read(() -> {
            var response = this.repository.findAll();
            return this.mapper.toDomain(response);
        });
    }

    @Override
    public Result<ContactType, Error> findById(int id) {
        return persistenceProxy.read(() -> this.repository.findById(id))
                .flatMap(maybeType -> maybeType
                        .map(this.mapper::toDomain)
                        .map(Result::<ContactType, Error>success)
                        .orElse(Result.failure(ErrorType.NOT_FOUND,
                                new Field("contact type", "There is no registered contact type with that ID")))
                );
    }
}
