package com.jobby.userservice.infrastructure.adapters.in.messaging;

import com.jobby.domain.ports.in.EventBus;
import com.jobby.domain.ports.transformations.TransformationRegistry;
import com.jobby.infrastructure.adapter.events.EventLogger;
import com.jobby.userservice.application.contracts.events.UserCreatedEvent;
import com.jobby.userservice.events.UserCreatedSchema;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class OwnerEventConsumer {

    private final EventBus eventBus;
    private final TransformationRegistry transformationRegistry;
    private final EventLogger eventLogger;

    @KafkaListener(
            topics = "com.jobby.user.create-user",
            groupId = "com.jobby.user-service.create-user-consumer"
    )
    public void onCreatedOwner(@Payload UserCreatedSchema userCreated, @Headers Map<String, Object> headers){
        eventLogger.logReceived(userCreated, headers);
        var result = this.transformationRegistry
                .get(UserCreatedSchema.class, UserCreatedEvent.class)
                .transform(userCreated)
                .flatMap(this.eventBus::dispatch);
        eventLogger.logResult(userCreated, headers, result);
    }
}
