package com.jobby.userservice.application.contracts.events;

import com.jobby.domain.ports.events.DomainEvent;
import com.jobby.domain.ports.events.OperationType;
import com.jobby.userservice.domain.models.vo.shared.Email;
import lombok.Getter;

@Getter
public class EmailChangeRequestEvent extends DomainEvent {
    private final Email email;
    private final String code;

    protected EmailChangeRequestEvent(long userId, Email email, String code) {
        super(Long.toString(userId), "email.change.request", OperationType.TRANSITIVE);
        this.email = email;
        this.code = code;
    }
}
