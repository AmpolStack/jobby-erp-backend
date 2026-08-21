# ADR-0020: Buses in-process Command/Query/Event sin reflexión

* Fecha: 2026-08-20 (retro-documentada)
* Estado: aceptada

## Contexto y problema

Con CQRS parcial ([ADR-0006](adr-0006-cqrs-parcial.md)), controllers necesitan despachar commands/queries sin conocer handlers concretos. El primer análisis del codebase detectó una violación de DIP: los controllers inyectaban la clase concreta del use case (`CreateOwnerUseCaseAdapter`) en lugar de abstraerse. Había que decidir el mecanismo de despacho.

## Impulsores de la decisión

* Inversión de dependencias real: controller → bus → handler, sin acoplamiento a clases concretas.
* Instrumentación centralizada: logging y métricas de todo el pipeline en un solo punto.
* Cero magia: resolver handlers debe ser legible y depurable.
* N handlers por evento (1:1 para command/query).

## Opciones consideradas

### Opción A: Inyección directa de handlers concretos

* Bien, porque simple y directo.
* Malo, porque es la violación detectada; instrumentación duplicada por controller; acoplamiento a implementación.

### Opción B: MediatR-style con escaneo classpath y reflexión

Auto-descubrimiento de handlers por reflexión/convención de nombres.

* Bien, porque cero registro manual.
* Malo, porque viola *explicitness over magic*: fallos de registro aparecen en runtime, el wiring es invisible, y la reflexión complica debugging y native images.

### Opción C: Buses in-process con registro explícito y decoradores

`CommandBus`, `QueryBus`, `EventBus` resuelven handlers por tipo de contrato desde un `Map<Class, Handler>` construido con registro fluido encadenado en la AutoConfiguration — cero reflexión. Decoradores composables envuelven cada bus:

```java
// Orden fijo: logging por fuera, métricas por dentro
CommandBus bus = new LoggingCommandBusDecorator(
                      new MetricsCommandBusDecorator(
                          new CommandBusImpl(handlers), meterRegistry));
```

Semántica por contrato: Command/Query retornan `Result<TResponse, Error>` (1 handler); Event es fire-and-forget `void` (N handlers).

* Bien, porque DIP real; pipeline instrumentado una sola vez; wiring visible y depurable; orden de decoradores explícito.
* Malo, porque registrar cada handler manualmente (costo asumido: una línea por handler); olvidar registrar = error en startup (deseable: fail-fast).

## Decisión

Opción elegida: **C**. Los controllers inyectan únicamente los buses. Los contratos son sealed markers ([ADR-0021](adr-0021-contratos-records-application.md)). Métricas del pipeline: Timer `command.bus.duration` con tags `command`/`success` ([ADR-0013](adr-0013-observabilidad-self-hosted.md)).

### Justificación

El registro explícito convierte un posible error runtime en error de compilación/startup y hace el sistema completo trazable leyendo una única clase de configuración. Es la materialización más pura del principio [ADR-0039](adr-0039-principio-explicitness-over-magic.md).

## Consecuencias

* **Positivas**: instrumentación uniforme; DIP verificado; fail-fast ante handlers no registrados; tests de buses triviales.
* **Negativas**: línea de registro por handler (aceptado deliberadamente); tres buses que mantener (comparten estructura).
* **Neutras**: el consumidor Kafka reutiliza el EventBus para eventos entrantes ([guía de mensajería](../../internal/guides/messaging-guide.md)).

## Validación

Tests unitarios de los tres buses; métricas visibles en Grafana; review verifica que ningún controller inyecte handlers concretos.

## Referencias

* [Patrón público: CQRS y buses](../patterns/cqrs-buses.md) · [ADR-0039](adr-0039-principio-explicitness-over-magic.md)
