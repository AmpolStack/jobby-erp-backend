package com.jobby.userservice.infrastructure.adapters.in;

import com.jobby.infrastructure.response.definition.HttpResponseProcessor;
import com.jobby.userservice.application.useCases.RemoveProfileImageUseCase;
import com.jobby.userservice.application.useCases.UpdateProfileImageUseCase;
import com.jobby.userservice.infrastructure.adapters.in.mappers.UserHttpMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {

    private final UpdateProfileImageUseCase updateProfileImage;
    private final RemoveProfileImageUseCase removeProfileImage;

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

    @PatchMapping("/{id}/email")
    public ResponseEntity<?> updateEmail(@PathVariable long id){
        return GenericUnimplementedResponse.unimplemented();
    }

    @PatchMapping("/{id}/profile-picture")
    public ResponseEntity<?> updateProfilePicture(@PathVariable long id,
                                                  @RequestParam("file") MultipartFile file){
        var finalResponse = this.mapper.toUploadImageCommand(id, file)
                .flatMap(this.updateProfileImage::execute);
        return response.map(finalResponse);
    }

    @DeleteMapping("/{id}/profile-picture")
    public ResponseEntity<?> DeleteProfilePicture(@PathVariable long id){
        var finalResponse = this.removeProfileImage.execute(id);
        return response.map(finalResponse);
    }
}
