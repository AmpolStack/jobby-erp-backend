package com.jobby.userservice.infrastructure.adapters.in.mappers;

import com.jobby.userservice.application.commands.CreateOwnerCommand;
import com.jobby.userservice.application.commands.CreateUserCommand;
import com.jobby.userservice.application.commands.UpdateRecoveryEmailCommand;
import com.jobby.userservice.application.queries.GetContactQuery;
import com.jobby.userservice.application.queries.GetOwnerQuery;
import com.jobby.userservice.application.queries.GetUserQuery;
import com.jobby.userservice.infrastructure.adapters.in.requests.CreateOwnerRequest;
import com.jobby.userservice.infrastructure.adapters.in.requests.UpdateRecoveryEmailRequest;
import com.jobby.userservice.infrastructure.adapters.in.responses.ContactResponse;
import com.jobby.userservice.infrastructure.adapters.in.responses.OwnerResponse;
import com.jobby.userservice.infrastructure.adapters.in.responses.UserResponse;
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


    ContactResponse toContactResponse(GetContactQuery query);

    UserResponse toUserResponse(GetUserQuery query);

    OwnerResponse toOwnerResponse(GetOwnerQuery query);
}

