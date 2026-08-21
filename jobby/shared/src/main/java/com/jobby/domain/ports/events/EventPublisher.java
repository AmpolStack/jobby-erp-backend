package com.jobby.domain.ports.events;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;

public interface EventPublisher {
    Result<Void, Error> send(DomainEvent event);
}
