package com.jobby.userservice.infrastructure.mappers.common;

import com.jobby.userservice.domain.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface DomainVOMapper {

    @Named("fromContactValue")
    default String fromContactValue(ContactValue contactValue){
        if(contactValue == null){
            return null;
        }
        return contactValue.getValue();
    }

    @Named("fromPhone")
    default String fromPhone(Phone phone){
        if(phone == null){
            return null;
        }
        return phone.getNumber();
    }

    @Named("fromEmail")
    default String fromEmail(Email email){
        if(email == null){
            return null;
        }
        return email.getEmail();
    }

    @Named("fromName")
    default String fromName(Name name){
        if(name == null){
            return null;
        }
        return name.getValue();
    }

    @Named("fromIdentificationNumber")
    default String fromIdentificationNumber(IdentificationNumber identificationNumber){
        if(identificationNumber == null){
            return null;
        }
        return identificationNumber.getNumber();
    }

    @Named("fromImageUrl")
    default String fromImageUrl(ImageUrl imageUrl){
        if(imageUrl == null){
            return null;
        }
        return imageUrl.getValue();
    }
}
