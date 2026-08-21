package com.jobby.userservice.infrastructure.adapters.in.rest.controllers;

import com.jobby.domain.ports.SafeResultValidator;
import com.jobby.domain.ports.in.CommandBus;
import com.jobby.domain.ports.in.QueryBus;
import com.jobby.infrastructure.response.definition.HttpResponseProcessor;
import com.jobby.userservice.application.contracts.commands.RemoveRecoveryEmailCommand;
import com.jobby.userservice.application.contracts.queries.GetOwnerByIdQuery;
import com.jobby.userservice.application.responses.OwnerResponse;
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

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final HttpResponseProcessor response;
    private final OwnerHttpMapper ownerHttpMapper;
    private final SafeResultValidator validator;

    @PostMapping("/")
    public ResponseEntity<?> createOwner(@RequestBody CreateOwnerRequest request){
        var finalResponse = this.validator.validate(request)
                .flatMap(v -> {
                    var command = this.ownerHttpMapper.toOwnerCommand(request);
                    return this.commandBus.<OwnerResponse>dispatch(command);
                })
                .map(this.ownerHttpMapper::toOwnerResponse);

        return this.response.map(finalResponse);
    }

    @PatchMapping("/recovery-email")
    public ResponseEntity<?> updateAlternativeEmail(@RequestBody UpdateRecoveryEmailRequest request){
        var finalResponse = this.validator.validate(request)
                .flatMap(v -> {
                    var command = this.ownerHttpMapper.toUpdateRecoveryCommand(request);
                    return this.commandBus.<OwnerResponse>dispatch(command);
                })
                .map(this.ownerHttpMapper::toOwnerResponse);

        return this.response.map(finalResponse);
    }

    @DeleteMapping("/{ownerId}/recovery-email")
    public ResponseEntity<?> removeAlternativeEmail(@PathVariable long ownerId){
        var finalResponse = this.commandBus.<OwnerResponse>dispatch(new RemoveRecoveryEmailCommand(ownerId))
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
        var finalResponse = this.queryBus.<OwnerResponse>dispatch(new GetOwnerByIdQuery(ownerId))
                .map(this.ownerHttpMapper::toOwnerResponse);

        return this.response.map(finalResponse);
    }
}

