package com.jobby.userservice.infrastructure.adapters.in;

import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.domain.ports.IdGenerator;
import com.jobby.userservice.application.commands.CreateOwnerCommand;
import com.jobby.userservice.application.commands.CreateUserCommand;
import com.jobby.userservice.application.useCases.CreateOwnerUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/owner")
public class OwnerController {

    private final CreateOwnerUseCase createOwnerUseCase;

    public OwnerController(CreateOwnerUseCase createOwnerUseCase) {
        this.createOwnerUseCase = createOwnerUseCase;
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthy(){
        var response = ValidationChain.create()
                .validateIf(false, () -> Result.failure(ErrorType.VALIDATION_ERROR, new Field("phone", "the provided phone is invalid")))
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/")
    public ResponseEntity<?> createOwner(@RequestBody CreateOwnerCommand command){
        var response = createOwnerUseCase.execute(command);
        return ResponseEntity.ok(response);
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
