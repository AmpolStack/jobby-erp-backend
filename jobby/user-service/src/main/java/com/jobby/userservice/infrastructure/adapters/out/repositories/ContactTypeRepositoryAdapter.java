package com.jobby.userservice.infrastructure.adapters.out.repositories;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.models.ContactType;
import com.jobby.userservice.domain.ports.ContactTypeRepository;
import com.jobby.userservice.infrastructure.mappers.entities.MongoContactTypeMapper;
import com.jobby.userservice.infrastructure.persistence.repository.SpringDataMongoContactTypesRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ContactTypeRepositoryAdapter implements ContactTypeRepository {

    private final SpringDataMongoContactTypesRepository repository;
    private final MongoContactTypeMapper mapper;

    public ContactTypeRepositoryAdapter(SpringDataMongoContactTypesRepository repository, MongoContactTypeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Result<List<ContactType>, Error> findAll() {
        var response = this.repository.findAll();
        var mapped = this.mapper.toDomain(response);
        return Result.success(mapped);
    }

    @Override
    public Result<ContactType, Error> findById(int id) {
        return this.repository.findById(id)
                .map(this.mapper::toDomain)
                .map(Result::<ContactType,Error>success)
                .orElse(Result.failure(ErrorType.NOT_FOUND,
                        new Field("contact type", "There is no registered contact type with that ID")));
    }
}
