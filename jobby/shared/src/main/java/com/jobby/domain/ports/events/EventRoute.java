package com.jobby.domain.ports.events;

public record EventRoute<D>(String route, Class<D> destinyClass) {
}
