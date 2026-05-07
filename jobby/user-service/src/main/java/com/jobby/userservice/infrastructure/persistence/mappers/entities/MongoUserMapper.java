package com.jobby.userservice.infrastructure.persistence.mappers.entities;

import com.jobby.infrastructure.security.fields.IndexedField;
import com.jobby.infrastructure.security.fields.ProtectedField;
import com.jobby.userservice.domain.models.User;
import com.jobby.userservice.domain.vo.Email;
import com.jobby.userservice.domain.vo.IdentificationNumber;
import com.jobby.userservice.domain.vo.Name;
import com.jobby.userservice.domain.vo.Phone;
import com.jobby.userservice.infrastructure.persistence.mappers.common.DomainVOMapper;
import com.jobby.userservice.infrastructure.persistence.mappers.common.SecuredFieldMapper;
import com.jobby.userservice.infrastructure.persistence.entities.MongoUserEntity;
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
                domainVOMapper.toName(securedFieldMapper.fromProtectedField(entity.getFirstName())),
                domainVOMapper.toName(securedFieldMapper.fromProtectedField(entity.getLastName())),
                entity.getRole(),
                entity.isActive(),
                domainVOMapper.toImageUrl(entity.getProfileImageUrl()),
                domainVOMapper.toIdentificationNumber(securedFieldMapper.fromIndexedField(entity.getIdentificationNumber())),
                domainVOMapper.toEmail(securedFieldMapper.fromIndexedField(entity.getEmail())),
                domainVOMapper.toPhone(securedFieldMapper.fromIndexedField(entity.getPhone())),
                entity.getCreatedAt(),
                entity.getModifiedAt());
    }


    @Mapping(target = "firstName", source = "firstName", qualifiedByName = "fromNameToProtectedField")
    @Mapping(target = "lastName", source = "lastName", qualifiedByName = "fromNameToProtectedField")
    @Mapping(target = "profileImageUrl", source = "profileImageUrl", qualifiedByName = "fromImageUrl")
    @Mapping(target = "identificationNumber", source = "identificationNumber", qualifiedByName = "fromIdentificationNumberToIndexedField")
    @Mapping(target = "phone", source = "phone", qualifiedByName = "fromPhoneToIndexedField")
    @Mapping(target = "email", source = "email", qualifiedByName = "fromEmailToIndexedField")
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
