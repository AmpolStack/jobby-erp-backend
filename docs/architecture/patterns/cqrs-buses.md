# CQRS y Buses in-process

* Guía educativa · Decisiones: [ADR-0006](../adr/adr-0006-cqrs-parcial.md), [ADR-0020](../adr/adr-0020-buses-in-process-command-query-event.md), [ADR-0021](../adr/adr-0021-contratos-records-application.md)

---

## El problema

Un caso de uso de escritura y una consulta tienen requisitos opuestos: la escritura exige invariantes estrictas; la lectura exige velocidad. Mezclarlos produce servicios que son lentos para leer (validan de más) y frágiles para escribir (no validan lo suficiente). Además, si los controllers inyectan handlers concretos, cada endpoint está acoplado a una implementación — imposible instrumentar el pipeline globalmente.

## La idea central

**Separar la intención en tres contratos:**

| Contrato | Semántica | Handlers | Retorna |
|---|---|---|---|
| **Command** | Intención que muta estado | 1:1 | `Result<TResponse, Error>` |
| **Query** | Pregunta que lee | 1:1 | `Result<TResponse, Error>` |
| **Event** | Hecho que ya ocurrió | N (fire-and-forget) | `void` |

Y despacharlos por **buses in-process** con registro explícito:

```java
// El controller no conoce handlers — solo el bus
validator.validate(request)
    .flatMap(v -> commandBus.dispatch(new CreateOwnerCommand(...)))
    .map(responseProcessor::process);
```

```java
// Registro explícito, cero reflexión (explicitness over magic)
commandBus.register(CreateOwnerCommand.class, createOwnerHandler);
```

## CQRS *parcial*: la decisión clave

CQRS completo implica modelos de lectura desnormalizados sincronizados por eventos. Este proyecto adopta **CQRS parcial**: commands/queries separados como contratos, pero mismo modelo de dominio.

¿Por qué? El análisis inicial encontró 8 commands vs 1 query — un sistema dominado por escritura. Construir proyecciones de lectura habría sido resolver un problema inexistente pagando costos reales de consistencia eventual. La puerta queda abierta: si las métricas del QueryBus muestran latencias degradadas, se abre ADR de proyecciones.

## El pipeline instrumentado

Los buses son decorables — logging y métricas se aplican una sola vez para todo el sistema:

```java
// Orden fijo: logging por fuera, métricas por dentro
new LoggingCommandBusDecorator(
    new MetricsCommandBusDecorator(
        new CommandBusImpl(handlers), meterRegistry));
```

Cada dispatch produce Timer `command.bus.duration` con tags `command`/`success` — visibilidad total sin tocar un controller.

## Trade-offs honestos

| A favor | En contra |
|---|---|
| Intención explícita por contrato (sealed records) | Línea de registro manual por handler |
| Instrumentación centralizada gratis | Tres buses estructuralmente similares que mantener |
| Ruta clara hacia CQRS completo | Lecturas ricas atraviesan agregados completos |

**Cuándo NO usarlo:** apps pequeñas donde el controller puede llamar al servicio directamente sin perder claridad; el patrón paga cuando hay múltiples handlers, necesidad de pipeline transversal o evolución hacia proyecciones.

## Dónde verlo

- Buses y decoradores: `jobby/shared/src/main/java/com/jobby/infrastructure/bus/`
- Contratos: `user-service/application/contracts/{commands,queries,events,responses}`
- Wiring: `AutoConfiguration.java` de user-service
- Guía operativa: [`docs/internal/guides/architecture-rules.md`](../../internal/guides/architecture-rules.md)

## Para ir más profundo

- Greg Young — *CQRS Documents* (el origen del patrón)
- [ADR-0023](../adr/adr-0023-separacion-event-interno-domainevent-externo.md) — cómo los eventos internos se distinguen de los mensajes externos
