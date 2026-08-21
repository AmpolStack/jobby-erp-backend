package com.jobby.domain.ports.events;

import com.jobby.domain.functional.PersistenceTask;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;

public interface TransactionalEventPublisher {
    Result<PersistenceTask, Error> send(DomainEvent event);
}
