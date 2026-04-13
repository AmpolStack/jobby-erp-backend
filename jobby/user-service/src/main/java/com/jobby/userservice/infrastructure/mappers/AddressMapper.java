package com.jobby.userservice.infrastructure.mappers;

import com.jobby.userservice.application.mapper.MunicipalityMapper;
import com.jobby.userservice.domain.models.Address;
import com.jobby.userservice.infrastructure.persistence.entities.MongoAddressEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MunicipalityMapper.class})
public interface AddressMapper {

    @Mapping(target = "direction", ignore = true)
    Address toDomain(MongoAddressEntity entity);

    @Mapping(target = "direction", ignore = true)
    MongoAddressEntity toEntity(Address domain);
}
