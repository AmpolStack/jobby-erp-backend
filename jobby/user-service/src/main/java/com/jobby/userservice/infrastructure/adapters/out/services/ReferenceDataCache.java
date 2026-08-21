package com.jobby.userservice.infrastructure.adapters.out.services;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.models.reference.ContactType;
import com.jobby.userservice.domain.models.reference.Department;
import com.jobby.userservice.domain.models.reference.IdentificationType;
import com.jobby.userservice.domain.models.reference.Municipality;
import com.jobby.userservice.domain.ports.out.services.ReferenceDataProvider;
import com.jobby.userservice.infrastructure.persistence.entities.MongoContactTypeEntity;
import com.jobby.userservice.infrastructure.persistence.entities.MongoIdentificationTypeEntity;
import com.jobby.userservice.infrastructure.persistence.entities.MongoMunicipalityEntity;
import com.jobby.userservice.infrastructure.persistence.mappers.entities.MongoContactTypeMapper;
import com.jobby.userservice.infrastructure.persistence.mappers.entities.MongoIdentificationTypeMapper;
import com.jobby.userservice.infrastructure.persistence.mappers.entities.MongoMunicipalityMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReferenceDataCache implements ReferenceDataProvider {

    private final MongoTemplate mongoTemplate;
    private final MongoIdentificationTypeMapper identificationTypeMapper;
    private final MongoContactTypeMapper contactTypeMapper;
    private final MongoMunicipalityMapper municipalityMapper;

    private volatile Map<Integer, IdentificationType> identificationTypes = Map.of();
    private volatile Map<Integer, ContactType> contactTypes = Map.of();
    private volatile Map<Integer, Municipality> municipalities = Map.of();
    private volatile Map<Integer, Department> departments = Map.of();

    @PostConstruct
    public void load() {
        refresh();
    }

    public void refresh() {
        this.identificationTypes = mongoTemplate.findAll(MongoIdentificationTypeEntity.class)
                .stream().collect(Collectors.toUnmodifiableMap(
                        MongoIdentificationTypeEntity::getId,
                        identificationTypeMapper::toDomain));

        this.contactTypes = mongoTemplate.findAll(MongoContactTypeEntity.class)
                .stream().collect(Collectors.toUnmodifiableMap(
                        MongoContactTypeEntity::getId,
                        contactTypeMapper::toDomain));

        this.municipalities = mongoTemplate.findAll(MongoMunicipalityEntity.class)
                .stream().collect(Collectors.toUnmodifiableMap(
                        MongoMunicipalityEntity::getId,
                        municipalityMapper::toDomain));

        this.departments = this.municipalities.values().stream()
                .map(Municipality::getDepartment)
                .distinct()
                .collect(Collectors.toUnmodifiableMap(
                        Department::getId, Function.identity()));
    }

    @Override
    public Result<IdentificationType, Error> identificationType(int id) {
        var result = identificationTypes.get(id);
        return result != null
                ? Result.success(result)
                : Result.failure(ErrorType.VALIDATION_ERROR,
                        new Field("identification type", "There is no registered identification type with that ID"));
    }

    @Override
    public Result<ContactType, Error> contactType(int id) {
        var result = contactTypes.get(id);
        return result != null
                ? Result.success(result)
                : Result.failure(ErrorType.VALIDATION_ERROR,
                        new Field("contact type", "There is no registered contact type with that ID"));
    }

    @Override
    public Result<Municipality, Error> municipality(int id) {
        var result = municipalities.get(id);
        return result != null
                ? Result.success(result)
                : Result.failure(ErrorType.VALIDATION_ERROR,
                        new Field("municipality", "There is no registered municipality with that ID"));
    }

    @Override
    public Result<Department, Error> department(int id) {
        var result = departments.get(id);
        return result != null
                ? Result.success(result)
                : Result.failure(ErrorType.VALIDATION_ERROR,
                        new Field("department", "There is no registered department with that ID"));
    }
}
