package com.jobby.userservice.infrastructure.adapters.in.rest.controllers;

import com.jobby.domain.ports.SafeResultValidator;
import com.jobby.infrastructure.response.definition.HttpResponseProcessor;
import com.jobby.userservice.application.useCases.RemoveProfileImageUseCaseAdapter;
import com.jobby.userservice.application.useCases.UpdateProfileImageUseCaseAdapter;
import com.jobby.userservice.domain.contract.commands.RemoveProfileImageCommand;
import com.jobby.userservice.domain.ports.in.UpdateEmailUseCase;
import com.jobby.userservice.infrastructure.adapters.in.rest.mappers.UserHttpMapper;
import com.jobby.userservice.infrastructure.adapters.in.rest.requests.ChangeEmailRequest;
import com.jobby.userservice.infrastructure.adapters.in.rest.requests.ConfirmEmailRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {

    private final UpdateProfileImageUseCaseAdapter updateProfileImage;
    private final RemoveProfileImageUseCaseAdapter removeProfileImage;
    private final UpdateEmailUseCase updateEmail;

    private final SafeResultValidator validator;
    private final UserHttpMapper mapper;
    private final HttpResponseProcessor response;

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable long id){
        return GenericUnimplementedResponse.unimplemented();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable long id){
        return GenericUnimplementedResponse.unimplemented();
    }

    @PatchMapping("/change-email")
    public ResponseEntity<?> changeEmail(@RequestBody ChangeEmailRequest request){
        var finalResponse = this.validator.validate(request)
                .flatMap(v -> {
                    var command = this.mapper.toCommand(request);
                    return this.updateEmail.request(command);
                });

        return this.response.map(finalResponse);
    }

    @PatchMapping("/change-email/confirm")
    public ResponseEntity<?> confirmChangeEmail(@RequestBody ConfirmEmailRequest request){
        var finalResponse = this.validator.validate(request)
                .flatMap(v -> {
                    var command = this.mapper.toCommand(request);
                    return this.updateEmail.confirm(command);
                });

        return this.response.map(finalResponse);
    }


    @PatchMapping("/{id}/profile-picture")
    public ResponseEntity<?> updateProfilePicture(@PathVariable long id,
                                                  @RequestParam("file") MultipartFile file){
        var finalResponse = this.mapper.toCommand(id, file)
                .flatMap(this.updateProfileImage::execute);
        return response.map(finalResponse);
    }

    @DeleteMapping("/{id}/profile-picture")
    public ResponseEntity<?> DeleteProfilePicture(@PathVariable long id){
        var finalResponse = this.removeProfileImage.execute(new RemoveProfileImageCommand(id));
        return response.map(finalResponse);
    }
}
