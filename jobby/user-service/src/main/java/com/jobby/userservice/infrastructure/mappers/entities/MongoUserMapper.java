package com.jobby.userservice.infrastructure.mappers.entities;

import com.jobby.infrastructure.security.fields.IndexedField;
import com.jobby.infrastructure.security.fields.ProtectedField;
import com.jobby.userservice.domain.models.User;
import com.jobby.userservice.domain.vo.Email;
import com.jobby.userservice.domain.vo.IdentificationNumber;
import com.jobby.userservice.domain.vo.Name;
import com.jobby.userservice.domain.vo.Phone;
import com.jobby.userservice.infrastructure.mappers.common.DomainVOMapper;
import com.jobby.userservice.infrastructure.mappers.common.SecuredFieldMapper;
import com.jobby.userservice.infrastructure.persistence.entities.MongoUserEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {MongoContactMapper.class, SecuredFieldMapper.class, DomainVOMapper.class})
public abstract class MongoUserMapper {

    @Autowired
    protected SecuredFieldMapper securedFieldMapper;
    @Autowired
    protected MongoContactMapper mongoContactMapper;
    @Autowired
    protected DomainVOMapper domainVOMapper;

    public User toDomain(MongoUserEntity entity) {
        if(entity == null) return null;
        return User.reconstruct(entity.getId(),
                mongoContactMapper.toDomain(entity.getContacts()),
                entity.getIdentificationTypeId(),
                securedFieldMapper.fromProtectedField(entity.getFirstName()),
                securedFieldMapper.fromProtectedField(entity.getLastName()),
                entity.getRole(),
                entity.isActive(),
                entity.getProfileImageUrl(),
                securedFieldMapper.fromIndexedField(entity.getIdentificationNumber()),
                securedFieldMapper.fromIndexedField(entity.getEmail()),
                securedFieldMapper.fromIndexedField(entity.getPhone()),
                entity.getCreatedAt(),
                entity.getModifiedAt());
    }


    @Mapping(target = "firstName", source = "firstName", qualifiedByName = "fromNameToProtectedField")
    @Mapping(target = "lastName", source = "lastName", qualifiedByName = "fromNameToProtectedField")
    @Mapping(target = "profileImageUrl", source = "profileImageUrl", qualifiedByName = "fromImageUrl")
    @Mapping(target = "identificationNumber", source = "identificationNumber", qualifiedByName = "fromIdentificationNumberToIndexedField")
    @Mapping(target = "email", source = "email", qualifiedByName = "fromEmailToIndexedField")
    @Mapping(target = "phone", source = "phone", qualifiedByName = "fromPhoneToIndexedField")
    public abstract MongoUserEntity toEntity(User domain);

    @Named("fromNameToProtectedField")
    public ProtectedField fromNameToProtectedField(Name name){
        var str = this.domainVOMapper.fromName(name);
        return this.securedFieldMapper.toProtectedField(str);
    }

    @Named("fromIdentificationNumberToIndexedField")
    public IndexedField fromIdentificationNumberToIndexedField(IdentificationNumber identificationNumber){
        var str = this.domainVOMapper.fromIdentificationNumber(identificationNumber);
        return this.securedFieldMapper.toIndexedField(str);
    }

    @Named("fromEmailToIndexedField")
    public IndexedField fromEmailToIndexedField(Email email){
        var str = this.domainVOMapper.fromEmail(email);
        return this.securedFieldMapper.toIndexedField(str);
    }

    @Named("fromPhoneToIndexedField")
    public IndexedField fromPhoneToIndexedField(Phone phone){
        var str = this.domainVOMapper.fromPhone(phone);
        return this.securedFieldMapper.toIndexedField(str);
    }
}
