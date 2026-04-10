package com.jobby.userservice.infrastructure.adapters.in;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/owner")
public class OwnerController {

    @PostMapping("/{userId}")
    public ResponseEntity<?> createOwner(@PathVariable long userId,
                                         @RequestBody Object owner){
        return GenericUnimplementedResponse.unimplemented();
    }

    @PatchMapping("/{ownerId}/alt-email")
    public ResponseEntity<?> updateAlternativeEmail(@PathVariable long ownerId,
                                                    @RequestBody String email){
        return GenericUnimplementedResponse.unimplemented();
    }

    @DeleteMapping("/{ownerId}/alt-email")
    public ResponseEntity<?> removeAlternativeEmail(@PathVariable long ownerId){
        return GenericUnimplementedResponse.unimplemented();
    }

    @PatchMapping("/{ownerId}/security-params")
    public ResponseEntity<?> updateSecurityParams(@PathVariable long ownerId,
                                                    @RequestBody Object securityParams){
        return GenericUnimplementedResponse.unimplemented();
    }
}
