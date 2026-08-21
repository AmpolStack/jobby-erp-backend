package com.jobby.userservice.infrastructure.transformations;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.security.encrypt.EncryptionService;
import com.jobby.domain.ports.transformations.Transformation;
import com.jobby.domain.ports.transformations.TransformationRegistry;
import com.jobby.userservice.application.contracts.events.UserCreatedEvent;
import com.jobby.userservice.domain.models.vo.shared.Email;
import com.jobby.userservice.domain.models.vo.shared.Name;
import com.jobby.userservice.events.UserCreatedSchema;
import org.springframework.stereotype.Component;

@Component
public class CreatedUserAvroToCreatedUserEvent extends Transformation<UserCreatedSchema, UserCreatedEvent> {

    private final EncryptionService encryptionService;

    public CreatedUserAvroToCreatedUserEvent(TransformationRegistry registry,
                                                EncryptionService encryptionService) {
        super(registry);
        this.encryptionService = encryptionService;
    }

    @Override
    public Result<UserCreatedEvent, Error> transform(UserCreatedSchema origin) {
        return this.encryptionService.decryptFromBytes(origin.getEmail().array())
                .flatMap(email ->
                        this.encryptionService.decryptFromBytes(origin.getName().array())
                        .map(name -> new UserCreatedEvent(origin.getUserId(),
                                Name.reconstruct(name),
                                Email.reconstruct(email),
                                origin.getCreatedAt())));
    }
}
