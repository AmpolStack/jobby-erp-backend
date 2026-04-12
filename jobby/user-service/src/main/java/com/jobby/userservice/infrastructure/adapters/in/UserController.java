package com.jobby.userservice.infrastructure.adapters.in;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

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
    public ResponseEntity<?> updateProfilePicture(@PathVariable long id){
        return GenericUnimplementedResponse.unimplemented();
    }

    @DeleteMapping("/{id}/profile-picture")
    public ResponseEntity<?> DeleteProfilePicture(@PathVariable long id){
        return GenericUnimplementedResponse.unimplemented();
    }
}
