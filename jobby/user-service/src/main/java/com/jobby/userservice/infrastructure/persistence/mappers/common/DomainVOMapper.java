package com.jobby.userservice.infrastructure.persistence.mappers.common;

import com.jobby.userservice.domain.models.vo.shared.*;
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

    @Named("toName")
    default Name toName(String value) {
        return Name.reconstruct(value);
    }

    @Named("toEmail")
    default Email toEmail(String value) {
        return Email.reconstruct(value);
    }

    @Named("toPhone")
    default Phone toPhone(String value) {
        return Phone.reconstruct(value);
    }

    @Named("toIdentificationNumber")
    default IdentificationNumber toIdentificationNumber(String value) {
        return IdentificationNumber.reconstruct(value);
    }

    @Named("toImageUrl")
    default ImageUrl toImageUrl(String value) {
        return ImageUrl.reconstruct(value);
    }

    @Named("toContactValue")
    default ContactValue toContactValue(String value) {
        return ContactValue.reconstruct(value);
    }
}
