package com.jobby.userservice.infrastructure.mappers.entities;

import com.jobby.infrastructure.security.fields.IndexedField;
import com.jobby.userservice.domain.models.Owner;
import com.jobby.userservice.domain.vo.Email;
import com.jobby.userservice.infrastructure.mappers.common.DomainVOMapper;
import com.jobby.userservice.infrastructure.mappers.common.SecuredFieldMapper;
import com.jobby.userservice.infrastructure.persistence.entities.MongoOwnerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {SecuredFieldMapper.class, DomainVOMapper.class})
public abstract class MongoOwnerMapper {

    @Autowired
    protected SecuredFieldMapper securedFieldMapper;
    @Autowired
    protected DomainVOMapper domainVOMapper;

    public Owner toDomain(MongoOwnerEntity entity){
        if(entity == null) return null;
        return Owner.reconstruct(entity.getId(),
                entity.getUserId(),
                this.securedFieldMapper.fromIndexedField(entity.getAlternativeEmail()),
                entity.getSecureParameters(),
                entity.getCreatedAt(),
                entity.getModifiedAt());
    }

    @Mapping(target = "alternativeEmail", source = "alternativeEmail", qualifiedByName = "fromEmailToIndexedField")
    public abstract MongoOwnerEntity toEntity(Owner domain);

    @Named("fromEmailToIndexedField")
    public IndexedField fromEmailToIndexedField(Email email){
        var str = this.domainVOMapper.fromEmail(email);
        return this.securedFieldMapper.toIndexedField(str);
    }
}
