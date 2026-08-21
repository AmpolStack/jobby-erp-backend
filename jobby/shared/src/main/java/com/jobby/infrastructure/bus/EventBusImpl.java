package com.jobby.infrastructure.bus;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.in.Event;
import com.jobby.domain.ports.in.EventBus;
import com.jobby.domain.ports.in.EventHandler;

import java.util.HashMap;
import java.util.Map;

public class EventBusImpl implements EventBus {

    private final Map<Class<? extends Event>, EventHandler<?, ?>> handlers = new HashMap<>();

    public <TEvent extends Event, TResponse> EventBusImpl register(
            Class<TEvent> eventType, EventHandler<TEvent, TResponse> handler) {
        handlers.put(eventType, handler);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TResponse> Result<TResponse, Error> dispatch(Event event) {
        var handler = handlers.get(event.getClass());
        if (handler == null) {
            return Result.failure(ErrorType.ITS_OPERATION_ERROR,
                    new Field("event", "No handler registered for: " + event.getClass().getSimpleName()));
        }
        return ((EventHandler<Event, TResponse>) handler).execute(event);
    }
}
