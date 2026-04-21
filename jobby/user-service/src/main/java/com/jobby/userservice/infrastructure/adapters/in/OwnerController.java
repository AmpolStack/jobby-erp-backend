package com.jobby.userservice.infrastructure.adapters.in;

import com.jobby.infrastructure.response.definition.APIMapper;
import com.jobby.userservice.application.commands.CreateOwnerCommand;
import com.jobby.userservice.application.useCases.CreateOwnerUseCase;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/owner")
@AllArgsConstructor
public class OwnerController {

    private final CreateOwnerUseCase createOwnerUseCase;
    private final APIMapper apiMapper;


    @PostMapping("/")
    public ResponseEntity<?> createOwner(@RequestBody CreateOwnerCommand command){
        var response = createOwnerUseCase.execute(command);
        return apiMapper.map(response);
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
