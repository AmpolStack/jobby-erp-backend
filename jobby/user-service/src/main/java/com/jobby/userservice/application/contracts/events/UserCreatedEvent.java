package com.jobby.userservice.application.contracts.events;

import com.jobby.domain.ports.events.DomainEvent;
import com.jobby.domain.ports.events.OperationType;
import com.jobby.domain.ports.in.Event;
import com.jobby.userservice.domain.models.vo.shared.Email;
import com.jobby.userservice.domain.models.vo.shared.Name;
import lombok.Getter;
import java.time.Instant;

@Getter
public class UserCreatedEvent extends DomainEvent implements Event {
    private final Name name;
    private final Email email;
    private final Instant createdAt;

    public UserCreatedEvent(long userId, Name name, Email email, Instant createdAt) {
        super(Long.toString(userId), "user", OperationType.CREATE);
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }
}
