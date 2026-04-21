package com.jobby.userservice.infrastructure.adapters.out.repositories;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.models.IdentificationType;
import com.jobby.userservice.domain.ports.IdentificationTypeRepository;
import com.jobby.userservice.infrastructure.mappers.entities.MongoIdentificationTypeMapper;
import com.jobby.userservice.infrastructure.persistence.repository.SpringDataMongoIdentificationTypeRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IdentificationTypeRepositoryAdapter implements IdentificationTypeRepository {

    private final SpringDataMongoIdentificationTypeRepository repository;
    private final MongoIdentificationTypeMapper mapper;

    public IdentificationTypeRepositoryAdapter(SpringDataMongoIdentificationTypeRepository repository, MongoIdentificationTypeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Result<IdentificationType, Error> findById(int id) {
        return this.repository.findById(id)
                .map(this.mapper::toDomain)
                .map(Result::<IdentificationType, Error>success)
                .orElse(Result.failure(ErrorType.NOT_FOUND,
                        new Field("identification type", "There is no registered identification type with that ID")));
    }

    @Override
    public Result<List<IdentificationType>, Error> getAll() {
        var response = this.repository.findAll();
        var mapped = this.mapper.toDomain(response);
        return Result.success(mapped);
    }
}
