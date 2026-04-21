package com.jobby.userservice.infrastructure.mappers.entities;

import com.jobby.infrastructure.security.fields.ProtectedField;
import com.jobby.userservice.domain.models.Contact;
import com.jobby.userservice.domain.vo.ContactValue;
import com.jobby.userservice.infrastructure.mappers.common.DomainVOMapper;
import com.jobby.userservice.infrastructure.mappers.common.SecuredFieldMapper;
import com.jobby.userservice.infrastructure.persistence.entities.MongoContactEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {DomainVOMapper.class, SecuredFieldMapper.class})
public abstract class MongoContactMapper {

    @Autowired
    protected SecuredFieldMapper securedFieldMapper;
    @Autowired
    protected DomainVOMapper domainVOMapper;

    public Contact toDomain(MongoContactEntity entity) {
        if(entity==null) return null;
        return Contact.reconstruct(entity.getId(),
                entity.getContactTypeId(),
                entity.getName(),
                entity.getDescription(),
                entity.isPublic(),
                securedFieldMapper.fromProtectedField(entity.getValue()));
    }

    public abstract Set<Contact> toDomain(Set<MongoContactEntity> entities);

    @Mapping(target = "value", source = "value", qualifiedByName = "fromContactValueToProtectedField")
    public abstract MongoContactEntity toEntity(Contact domain);

    @Named("fromContactValueToProtectedField")
    public ProtectedField fromContactValueToProtectedField(ContactValue vo){
        var str = this.domainVOMapper.fromContactValue(vo);
        return this.securedFieldMapper.toProtectedField(str);
    }

}
