package com.jobby.infrastructure.adapter.events;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.MessagingPublisher;
import com.jobby.domain.ports.events.DomainEvent;
import com.jobby.domain.ports.events.EventPublisher;
import com.jobby.domain.ports.events.EventRegistry;
import com.jobby.domain.ports.events.EventRoute;
import com.jobby.domain.ports.transformations.TransformationRegistry;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class EventPublisherAdapter implements EventPublisher {

    private final MessagingPublisher messagingPublisher;
    private final EventRegistry eventRegistry;
    private final TransformationRegistry transformationRegistry;

    @Override
    public Result<Void, Error> send(DomainEvent event) {
        var eventRoute = this.eventRegistry.get(event.getClass());
        if (eventRoute == null) {
            return Result.failure(ErrorType.ITS_OPERATION_ERROR,
                    new Field("EventRegistry", "No route registered for " + event.getClass().getSimpleName()));
        }
        return sendWithCapture(event, eventRoute);
    }

    private <O extends DomainEvent, D> Result<Void, Error> sendWithCapture(O event, EventRoute<D> eventRoute) {
        Class<O> originClass = (Class<O>) event.getClass();
        var transformation = this.transformationRegistry.get(
                originClass,
                eventRoute.destinyClass()
        );
        return transformation.transform(event)
                .flatMap(transformed -> this.messagingPublisher.publish(
                        eventRoute.route(),
                        event.getAggregateId(),
                        transformed,
                        buildMetadata(event),
                        5));
    }

    private static Map<String, String> buildMetadata(DomainEvent event) {
        var metadata = new HashMap<String, String>();
        metadata.put("eventId", event.getEventId().toString());
        metadata.put("occurredOn", event.getOccurredOn().toString());
        metadata.put("aggregateType", event.getAggregateType());
        metadata.put("operationType", event.getOperationType().name());
        return metadata;
    }
}
