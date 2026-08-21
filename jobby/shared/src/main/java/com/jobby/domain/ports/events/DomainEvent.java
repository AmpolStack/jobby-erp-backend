package com.jobby.domain.ports.events;

import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class DomainEvent {
    private final UUID eventId;
    private final Instant occurredOn;
    private final String aggregateId;
    private final String aggregateType;
    private final OperationType operationType;

    protected DomainEvent(String aggregateId, String aggregateType, OperationType operationType) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.operationType = operationType;
    }
}
