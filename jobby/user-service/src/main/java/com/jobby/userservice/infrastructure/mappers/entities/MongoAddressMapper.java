package com.jobby.userservice.infrastructure.mappers.entities;

import com.jobby.userservice.domain.models.Address;
import com.jobby.userservice.infrastructure.mappers.common.SecuredFieldMapper;
import com.jobby.userservice.infrastructure.persistence.entities.MongoAddressEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {MongoMunicipalityMapper.class, SecuredFieldMapper.class})
public abstract class MongoAddressMapper {

    @Autowired
    protected MongoMunicipalityMapper mongoMunicipalityMapper;
    @Autowired
    protected SecuredFieldMapper securedFieldMapper;

    public Address toDomain(MongoAddressEntity entity){
        if(entity == null) return null;
        return Address.reconstruct(entity.getId(),
                this.mongoMunicipalityMapper.toDomain(entity.getMunicipality()),
                this.securedFieldMapper.fromProtectedField(entity.getDirection()),
                entity.getCreatedAt(),
                entity.getModifiedAt());
    }

    @Mapping(target = "direction", source = "direction", qualifiedByName = "toProtectedField")
    public abstract MongoAddressEntity toEntity(Address domain);
}
