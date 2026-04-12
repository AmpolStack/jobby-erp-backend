package com.jobby.userservice.infrastructure.adapters.in;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class GenericUnimplementedResponse {
    public static ResponseEntity<String> unimplemented(){
        return ResponseEntity
                .status(HttpStatus.NOT_IMPLEMENTED)
                .body("This method has not been implemented");
    }
}
