package com.jobby.userservice.infrastructure.transformations;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.security.encrypt.EncryptionService;
import com.jobby.domain.ports.transformations.Transformation;
import com.jobby.domain.ports.transformations.TransformationRegistry;
import com.jobby.userservice.application.contracts.events.UserCreatedEvent;
import com.jobby.userservice.events.UserCreatedSchema;
import org.springframework.stereotype.Component;
import java.nio.ByteBuffer;

@Component
public class CreatedUserEventToCreatedUserAvro extends Transformation<UserCreatedEvent, UserCreatedSchema> {

    private final EncryptionService encryptionService;

    public CreatedUserEventToCreatedUserAvro(TransformationRegistry registry,
                                             EncryptionService encryptionService) {
        super(registry);
        this.encryptionService = encryptionService;
    }

    @Override
    public Result<UserCreatedSchema, Error> transform(UserCreatedEvent origin) {
        var entity = new UserCreatedSchema();
        return this.encryptionService.encryptAsBytes(origin.getEmail().getEmail())
                .flatMap(emailEncrypted -> {
                    var emailEncryptedBuffer = ByteBuffer.wrap(emailEncrypted);
                    entity.setEmail(emailEncryptedBuffer);
                    return this.encryptionService.encryptAsBytes(origin.getName().getValue());
                })
                .map(nameEncrypted -> {
                    var nameEncryptedBuffer = ByteBuffer.wrap(nameEncrypted);
                    entity.setName(nameEncryptedBuffer);
                    entity.setCreatedAt(origin.getCreatedAt());
                    entity.setUserId(Long.parseLong(origin.getAggregateId()));
                    return entity;
                });
    }
}
