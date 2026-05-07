package com.jobby.userservice.infrastructure.adapters.in;

import com.jobby.domain.ports.SafeResultValidator;
import com.jobby.infrastructure.response.definition.HttpResponseProcessor;
import com.jobby.userservice.application.useCases.CreateOwnerUseCase;
import com.jobby.userservice.application.useCases.GetOwnerByIdUseCase;
import com.jobby.userservice.application.useCases.RemoveRecoveryEmailUseCase;
import com.jobby.userservice.application.useCases.UpdateRecoveryEmailUseCase;
import com.jobby.userservice.infrastructure.adapters.in.mappers.OwnerHttpMapper;
import com.jobby.userservice.infrastructure.adapters.in.requests.CreateOwnerRequest;
import com.jobby.userservice.infrastructure.adapters.in.requests.UpdateRecoveryEmailRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/owner")
@AllArgsConstructor
public class OwnerController {

    private final CreateOwnerUseCase createOwner;
    private final GetOwnerByIdUseCase getOwnerById;
    private final UpdateRecoveryEmailUseCase updateAlternativeEmail;
    private final RemoveRecoveryEmailUseCase removeRecoveryEmail;

    private final HttpResponseProcessor response;
    private final OwnerHttpMapper ownerHttpMapper;
    private final SafeResultValidator validator;

    @PostMapping("/")
    public ResponseEntity<?> createOwner(@RequestBody CreateOwnerRequest request){
        var finalResponse = this.validator.validate(request)
                .flatMap(v -> {
                    var command = this.ownerHttpMapper.toOwnerCommand(request);
                    return this.createOwner.execute(command);
                })
                .map(this.ownerHttpMapper::toOwnerResponse);

        return this.response.map(finalResponse);
    }

    @PatchMapping("/recovery-email")
    public ResponseEntity<?> updateAlternativeEmail(@RequestBody UpdateRecoveryEmailRequest request){
        var finalResponse = this.validator.validate(request)
                .flatMap(v -> {
                    var command = this.ownerHttpMapper.toUpdateRecoveryCommand(request);
                    return this.updateAlternativeEmail.execute(command);
                })
                .map(this.ownerHttpMapper::toOwnerResponse);

        return this.response.map(finalResponse);
    }

    @DeleteMapping("/{ownerId}/recovery-email")
    public ResponseEntity<?> removeAlternativeEmail(@PathVariable long ownerId){
        var finalResponse = this.removeRecoveryEmail.execute(ownerId)
                .map(this.ownerHttpMapper::toOwnerResponse);

        return this.response.map(finalResponse);
    }

    @PatchMapping("/{ownerId}/security-params")
    public ResponseEntity<?> updateSecurityParams(@PathVariable long ownerId,
                                                    @RequestBody Object securityParams){
        return GenericUnimplementedResponse.unimplemented();
    }

    @GetMapping("/{ownerId}")
    public ResponseEntity<?> getById(@PathVariable long ownerId){
        var finalResponse = this.getOwnerById.execute(ownerId)
                .map(this.ownerHttpMapper::toOwnerResponse);

        return this.response.map(finalResponse);
    }
}

