package com.jobby.domain.ports.events;

import java.util.HashMap;
import java.util.Map;

public class EventRegistry {
    private final Map<Class<? extends DomainEvent>, EventRoute<?>> eventMaps = new HashMap<>();

    public <O extends DomainEvent, D> EventRegistry register(Class<O> originClass,
                                                    EventRoute<D> eventRoute) {
        this.eventMaps.put(originClass, eventRoute);
        return this;
    }

    public EventRoute<?> get(Class<?> eventClass) {
        return this.eventMaps.get(eventClass);
    }
}
