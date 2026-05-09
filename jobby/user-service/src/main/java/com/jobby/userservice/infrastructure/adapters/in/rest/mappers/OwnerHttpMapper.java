package com.jobby.userservice.infrastructure.adapters.in.mappers;

import com.jobby.userservice.domain.commands.CreateOwnerCommand;
import com.jobby.userservice.domain.commands.CreateUserCommand;
import com.jobby.userservice.domain.commands.UpdateRecoveryEmailCommand;
import com.jobby.userservice.domain.responses.ContactResponse;
import com.jobby.userservice.domain.responses.OwnerResponse;
import com.jobby.userservice.domain.responses.UserResponse;
import com.jobby.userservice.infrastructure.adapters.in.requests.CreateOwnerRequest;
import com.jobby.userservice.infrastructure.adapters.in.requests.UpdateRecoveryEmailRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OwnerHttpMapper {

    default CreateUserCommand toUserCommand(CreateOwnerRequest request) {
        if (request == null || request.user() == null) return null;
        var u = request.user();
        return new CreateUserCommand(
                u.identificationTypeId(),
                request.organizationId(),
                u.sectionalId(),
                u.firstName(),
                u.lastName(),
                u.identificationNumber(),
                u.email(),
                u.phone()
        );
    }

    default CreateOwnerCommand toOwnerCommand(CreateOwnerRequest request) {
        if (request == null) return null;
        return new CreateOwnerCommand(toUserCommand(request),
                request.secureParameters());
    }

    default UpdateRecoveryEmailCommand toUpdateRecoveryCommand(UpdateRecoveryEmailRequest request){
        if( request == null) return null;
        return new UpdateRecoveryEmailCommand(request.id(), request.recoveryEmail());
    }


    com.jobby.userservice.infrastructure.adapters.in.responses.ContactResponse toContactResponse(ContactResponse query);

    com.jobby.userservice.infrastructure.adapters.in.responses.UserResponse toUserResponse(UserResponse query);

    com.jobby.userservice.infrastructure.adapters.in.responses.OwnerResponse toOwnerResponse(OwnerResponse query);
}

