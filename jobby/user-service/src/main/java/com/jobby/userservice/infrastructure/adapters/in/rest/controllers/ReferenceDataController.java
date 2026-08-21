package com.jobby.userservice.infrastructure.adapters.in.rest.controllers;

import com.jobby.userservice.infrastructure.adapters.out.services.ReferenceDataCache;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ReferenceDataController {

    private final ReferenceDataCache cache;

    @PostMapping("/refresh-reference-data")
    public ResponseEntity<String> refresh() {
        cache.refresh();
        return ResponseEntity.ok("Reference data reloaded from database");
    }
}
