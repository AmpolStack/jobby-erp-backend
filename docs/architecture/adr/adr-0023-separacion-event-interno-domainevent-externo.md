# ADR-0023: Separación Event interno vs DomainEvent externo

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

El sistema maneja dos clases de eventos que no deben confundirse: los que orquestan reacciones *dentro* del servicio (misma JVM) y los que comunican hechos *hacia otros servicios* (Kafka). Unificarlos acoplaría el modelo de dominio al transporte; distinguirlos mal duplicaría lógica de publicación.

Además: ¿quién decide el nombre externo del evento (`owner.created` vs `OwnerCreatedEvent_v2`)? Si el dominio lo supiera, cambiar un naming de integración tocaría el centro puro.

## Impulsores de la decisión

* El dominio produce **hechos**; la infraestructura decide **transporte y naming**.
* Cambiar el contrato de integración (topic, nombre, esquema Avro) no debe tocar dominio ni casos de uso.
* Los eventos in-process (N handlers, fire-and-forget) tienen semántica distinta a los mensajes de broker (serialización, retry, DLT).

## Opciones consideradas

### Opción A: Un solo tipo de evento para todo

* Bien, porque menos tipos.
* Malo, porque cada evento in-process necesitaría esquema serializable + registro en broker (o cada evento externo tendría que vivir sin él); naming de integración filtrado al dominio.

### Opción B: Dominio publica directamente a Kafka con nombres de negocio

* Bien, porque menos capas.
* Malo, porque acopla agregados/casos de uso a topics y formatos; renombrar `owner_created` → `owner.created.v2` sería un refactor de dominio.

### Opción C: Dos abstracciones + naming exclusivo de infraestructura

- **`Event`** (marker en `domain/ports/in`): evento in-process, despachado por EventBus ([ADR-0020](adr-0020-buses-in-process-command-query-event.md)), N handlers en la misma JVM.
- **`DomainEvent`**: hecho de negocio emitido por agregados ([ADR-0022](adr-0022-eventos-dominio-pull-domain-events.md)), publicado hacia afuera vía puerto `DomainEventPublisher`.
- **Naming externo solo en infraestructura**: `EventEnvelope` `{type, payload, occurredAt}` + `EventNameMapping` (mapa clase→nombre, ej. `owner.created`) viven en `infrastructure/messaging`. "El dominio produce hechos, la infraestructura los nombra."

* Bien, porque cambiar naming/topics/esquemas es un cambio de infraestructura puro; cada semántica tiene su mecanismo; el consumidor genérico delega por tipo a handlers registrados.
* Malo, porque dos vocabularios de evento que documentar; transformación explícita Event→envelope→Avro que mantener.

## Decisión

Opción elegida: **C**. Flujo completo:

```
aggregate.behavior() → acumula DomainEvent
use case → transacción → pullDomainEvents() → DomainEventPublisher (puerto)
infrastructure → EventEnvelope + EventNameMapping → TransformationRegistry → Avro → Kafka
consumer → envelope → Transformation inversa → Event de aplicación → EventBus → handlers
```

Los DomainEvents son records con primitivos/String únicamente — serializables sin acoplarse a entidades de persistencia.

### Justificación

La separación materializa la frontera hexagonal dentro del propio concepto de "evento": lo que el negocio declara (hecho) y lo que la infraestructura resuelve (transporte, nombre, formato) evolucionan independiente. Es la pieza que hace viable cambiar el contrato de integración sin tocar una línea de dominio.

## Consecuencias

* **Positivas**: naming de integración mutable sin refactor de dominio; handlers in-process desacoplados del broker; transporte intercambiable (Kafka ↔ Spring Events locales vía `@Profile`/`@ConditionalOnProperty`).
* **Negativas**: tres representaciones por flujo (DomainEvent → envelope/Avro → Event de aplicación) con transformaciones explícitas.
* **Neutras**: la dualidad con Avro se detalla en [ADR-0010](adr-0010-avro-build-time-eventos.md).

## Validación

Tests de transformación bidireccional por evento; verificación de que ningún archivo de `domain/` menciona topics o nombres externos.

## Referencias

* [Patrón público: Domain Events](../patterns/domain-events.md) · [Guía interna de mensajería](../../internal/guides/messaging-guide.md)
