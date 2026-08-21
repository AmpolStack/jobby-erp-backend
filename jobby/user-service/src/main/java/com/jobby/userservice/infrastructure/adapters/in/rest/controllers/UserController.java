package com.jobby.userservice.infrastructure.adapters.in.rest.controllers;

import com.jobby.domain.ports.SafeResultValidator;
import com.jobby.domain.ports.in.CommandBus;
import com.jobby.infrastructure.response.definition.HttpResponseProcessor;
import com.jobby.userservice.application.contracts.commands.RemoveProfileImageCommand;
import com.jobby.userservice.application.responses.UserResponse;
import com.jobby.userservice.infrastructure.adapters.in.rest.mappers.UserHttpMapper;
import com.jobby.userservice.infrastructure.adapters.in.rest.requests.ChangeEmailRequest;
import com.jobby.userservice.infrastructure.adapters.in.rest.requests.ConfirmEmailRequest;
import com.jobby.userservice.infrastructure.adapters.in.rest.requests.UpdateUserRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {

    private final CommandBus commandBus;
    private final SafeResultValidator validator;
    private final UserHttpMapper mapper;
    private final HttpResponseProcessor response;

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable long id,
                                    @RequestBody UpdateUserRequest request){
        var finalResponse = this.validator.validate(request)
                .flatMap(v -> {
                    var command = this.mapper.toCommand(id, request);
                    return this.commandBus.<UserResponse>dispatch(command);
                });
        return this.response.map(finalResponse);
    }

    // TODO: Move to logic with permissions
    @PatchMapping("/{id}/status/{isActive}")
    public ResponseEntity<?> updateStatus(@PathVariable long id, @PathVariable boolean isActive){
        var command = this.mapper.toCommand(id, isActive);
        var finalResponse =  this.commandBus.dispatch(command);
        return this.response.map(finalResponse);
    }

    @PatchMapping("/change-email")
    public ResponseEntity<?> changeEmail(@RequestBody ChangeEmailRequest request){
        var finalResponse = this.validator.validate(request)
                .flatMap(v -> {
                    var command = this.mapper.toCommand(request);
                    return this.commandBus.<Void>dispatch(command);
                });

        return this.response.map(finalResponse);
    }

    @PatchMapping("/change-email/confirm")
    public ResponseEntity<?> confirmChangeEmail(@RequestBody ConfirmEmailRequest request){
        var finalResponse = this.validator.validate(request)
                .flatMap(v -> {
                    var command = this.mapper.toCommand(request);
                    return this.commandBus.<UserResponse>dispatch(command);
                });

        return this.response.map(finalResponse);
    }


    @PatchMapping("/{id}/profile-picture")
    public ResponseEntity<?> updateProfilePicture(@PathVariable long id,
                                                  @RequestParam("file") MultipartFile file){
        var finalResponse = this.mapper.toCommand(id, file)
                .flatMap(this.commandBus::dispatch);
        return response.map(finalResponse);
    }

    @DeleteMapping("/{id}/profile-picture")
    public ResponseEntity<?> DeleteProfilePicture(@PathVariable long id){
        var finalResponse = this.commandBus.dispatch(new RemoveProfileImageCommand(id));
        return response.map(finalResponse);
    }
}
