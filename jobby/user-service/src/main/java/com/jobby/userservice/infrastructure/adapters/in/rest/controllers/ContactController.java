package com.jobby.userservice.infrastructure.adapters.in;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/{userId}/contact")
public class ContactController {

    @PostMapping("/")
    public ResponseEntity<?> createContact(@PathVariable long userId,
                                           @RequestBody Object contact){
        return GenericUnimplementedResponse.unimplemented();
    }

    @PutMapping("/{contactId}")
    public ResponseEntity<?> updateContact(@PathVariable long contactId,
                                           @PathVariable long userId,
                                           @RequestBody Object contact){
        return GenericUnimplementedResponse.unimplemented();
    }

    @PatchMapping("/{contactId}/privacy")
    public ResponseEntity<?> changeContactPrivacy(@PathVariable long contactId,
                                                  @PathVariable long userId,
                                                  @RequestParam boolean isPublic){
        return GenericUnimplementedResponse.unimplemented();
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<?> deleteContact(@PathVariable long contactId,
                                           @PathVariable long userId){
        return GenericUnimplementedResponse.unimplemented();
    }
}
