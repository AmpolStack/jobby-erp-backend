package com.jobby.userservice.infrastructure.adapters.in.rest.mappers;

import com.jobby.userservice.domain.contract.commands.CreateOwnerCommand;
import com.jobby.userservice.domain.contract.commands.CreateUserCommand;
import com.jobby.userservice.domain.contract.commands.UpdateRecoveryEmailCommand;
import com.jobby.userservice.domain.contract.responses.ContactResponse;
import com.jobby.userservice.domain.contract.responses.OwnerResponse;
import com.jobby.userservice.domain.contract.responses.UserResponse;
import com.jobby.userservice.infrastructure.adapters.in.rest.requests.CreateOwnerRequest;
import com.jobby.userservice.infrastructure.adapters.in.rest.requests.UpdateRecoveryEmailRequest;
import com.jobby.userservice.infrastructure.adapters.in.rest.responses.ContactHttpResponse;
import com.jobby.userservice.infrastructure.adapters.in.rest.responses.OwnerHttpResponse;
import com.jobby.userservice.infrastructure.adapters.in.rest.responses.UserHttpResponse;
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

    ContactHttpResponse toContactResponse(ContactResponse query);

    UserHttpResponse toUserResponse(UserResponse query);

    OwnerHttpResponse toOwnerResponse(OwnerResponse query);
}
