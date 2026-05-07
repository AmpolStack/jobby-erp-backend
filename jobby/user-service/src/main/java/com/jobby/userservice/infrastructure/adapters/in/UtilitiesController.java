package com.jobby.userservice.infrastructure.adapters.in;

import com.jobby.domain.ports.IdGenerator;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/util")
@AllArgsConstructor
public class UtilitiesController {

    private final IdGenerator idGenerator;

    @GetMapping("/id")
    public ResponseEntity<?> getId(){
        var response = this.idGenerator.next();
        return ResponseEntity.ok(response);
    }
}
