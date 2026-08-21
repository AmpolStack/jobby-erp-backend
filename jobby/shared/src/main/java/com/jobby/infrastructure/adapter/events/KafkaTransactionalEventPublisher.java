package com.jobby.infrastructure.adapter.events;

import com.jobby.domain.functional.PersistenceTask;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.MessagingPublisher;
import com.jobby.domain.ports.events.DomainEvent;
import com.jobby.domain.ports.events.EventRegistry;
import com.jobby.domain.ports.events.EventRoute;
import com.jobby.domain.ports.events.TransactionalEventPublisher;
import com.jobby.domain.ports.transformations.TransformationRegistry;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class KafkaTransactionalEventPublisher implements TransactionalEventPublisher {
    private final MessagingPublisher messagingPublisher;
    private final EventRegistry eventRegistry;
    private final TransformationRegistry transformationRegistry;

    @Override
    public Result<PersistenceTask, Error> send(DomainEvent event) {
        var eventRoute = this.eventRegistry.get(event.getClass());
        if (eventRoute == null) {
            return Result.failure(ErrorType.ITS_OPERATION_ERROR,
                    new Field("EventRegistry", "No route registered for " + event.getClass().getSimpleName()));
        }
        return sendWithCapture(event, eventRoute);
    }

    private <O extends DomainEvent, D> Result<PersistenceTask, Error> sendWithCapture(O event, EventRoute<D> eventRoute) {
        Class<O> originClass = (Class<O>) event.getClass();
        var transformation = this.transformationRegistry.get(
                originClass,
                eventRoute.destinyClass()
        );

        return transformation.transform(event)
                .flatMap(transformed -> {
                    PersistenceTask task = () -> this.messagingPublisher.publish(eventRoute.route(), event.getAggregateId(), transformed, 5);
                    return Result.success(task);
                });
    }
}
