package com.jobby.userservice.infrastructure.adapters.in.rest.controllers;

import com.jobby.domain.ports.SafeResultValidator;
import com.jobby.infrastructure.response.definition.HttpResponseProcessor;
import com.jobby.userservice.application.useCases.CreateOwnerUseCaseAdapter;
import com.jobby.userservice.application.useCases.GetOwnerByIdUseCaseAdapter;
import com.jobby.userservice.application.useCases.RemoveRecoveryEmailUseCaseAdapter;
import com.jobby.userservice.application.useCases.UpdateRecoveryEmailUseCaseAdapter;
import com.jobby.userservice.domain.contract.commands.RemoveRecoveryEmailCommand;
import com.jobby.userservice.domain.contract.queries.GetOwnerByIdQuery;
import com.jobby.userservice.infrastructure.adapters.in.rest.mappers.OwnerHttpMapper;
import com.jobby.userservice.infrastructure.adapters.in.rest.requests.CreateOwnerRequest;
import com.jobby.userservice.infrastructure.adapters.in.rest.requests.UpdateRecoveryEmailRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/owner")
@AllArgsConstructor
public class OwnerController {

    private final CreateOwnerUseCaseAdapter createOwner;
    private final GetOwnerByIdUseCaseAdapter getOwnerById;
    private final UpdateRecoveryEmailUseCaseAdapter updateAlternativeEmail;
    private final RemoveRecoveryEmailUseCaseAdapter removeRecoveryEmail;

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
        var finalResponse = this.removeRecoveryEmail.execute(new RemoveRecoveryEmailCommand(ownerId))
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
        var finalResponse = this.getOwnerById.execute(new GetOwnerByIdQuery(ownerId))
                .map(this.ownerHttpMapper::toOwnerResponse);

        return this.response.map(finalResponse);
    }
}

