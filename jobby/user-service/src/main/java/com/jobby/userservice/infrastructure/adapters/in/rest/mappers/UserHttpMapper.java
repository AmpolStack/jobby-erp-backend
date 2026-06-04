package com.jobby.userservice.infrastructure.adapters.in.rest.mappers;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.contract.commands.ConfirmEmailChangeCommand;
import com.jobby.userservice.domain.contract.commands.RequestEmailChangeCommand;
import com.jobby.userservice.domain.contract.commands.UpdateIProfileImageCommand;
import com.jobby.userservice.infrastructure.adapters.in.rest.requests.ChangeEmailRequest;
import com.jobby.userservice.infrastructure.adapters.in.rest.requests.ConfirmEmailRequest;
import com.jobby.userservice.infrastructure.adapters.in.rest.responses.UserResponse;
import org.mapstruct.Mapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Mapper(componentModel = "spring")
public interface UserHttpMapper {
    default Result<UpdateIProfileImageCommand, Error> toCommand(long id, MultipartFile file){
        try {
            var response = new UpdateIProfileImageCommand(id,
                    file.getOriginalFilename(),
                    file.getBytes(),
                    file.getSize(),
                    file.getContentType());
            return Result.success(response);
        } catch (IOException e) {
            return Result.failure(ErrorType.INVALID_INPUT, new Field("file", "The file cannot be accessed"));
        }
    }

    default RequestEmailChangeCommand toCommand(ChangeEmailRequest request){
        if(request == null) return null;
        return new RequestEmailChangeCommand(request.userId(), request.email());
    }

    default ConfirmEmailChangeCommand toCommand(ConfirmEmailRequest request){
        if(request == null) return null;
        return new ConfirmEmailChangeCommand(request.userId(), request.code());
    }
}
