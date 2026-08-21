# Guía de Eventos de Dominio

* Tipo: guía interna (how-to) · Audiencia: contribuyentes · Última actualización: 2026-08-20
* Decisiones subyacentes: [ADR-0022](../../architecture/adr/adr-0022-eventos-dominio-pull-domain-events.md), [ADR-0023](../../architecture/adr/adr-0023-separacion-event-interno-domainevent-externo.md), [ADR-0024](../../architecture/adr/adr-0024-outbox-pattern-atomicidad-bd-kafka.md), [ADR-0025](../../architecture/adr/adr-0025-transacciones-explicitas-transaction-orchestrator.md)

> Este documento unifica el contenido de los tres análisis históricos
> (`domain-events-design`, `domain-events-collector-implementation`,
> `domain-events-pull-events-implementation`). Las decisiones viven en los ADRs;
> aquí vive el how-to.

---

## El flujo completo

```
aggregate.behavior() ──► acumula DomainEvent (lista interna oculta)
use case ────────────► transacción exitosa
use case ────────────► pullDomainEvents() → DomainEventPublisher (puerto)
infrastructure ──────► EventEnvelope + EventNameMapping → Transformation → Avro → Kafka
consumer ────────────► envelope → Transformation inversa → Event → EventBus → handlers
```

---

## 1. Emitir eventos desde el agregado

```java
// Dentro del aggregate — la lista es final y SIN getter público
private final List<DomainEvent> domainEvents = new ArrayList<>(); // @Getter(AccessLevel.NONE)

public static Result<User, Error> create(...) {
    // ... validación, construcción ...
    var user = new User(...);
    user.domainEvents.add(new UserCreatedEvent(...));  // el hecho nace donde ocurre
    return Result.success(user);
}

public List<DomainEvent> pullDomainEvents() {
    var events = List.copyOf(domainEvents);
    domainEvents.clear();
    return events;
}
```

**Reglas inquebrantables:**

1. `reconstruct()` **jamás** emite eventos — reconstruir desde BD no es acción de negocio (si emitiera `OwnerCreated`, cada lectura regeneraría el email de bienvenida).
2. Los DomainEvents son records con primitivos/String únicamente: inmutables, hechos en tiempo pasado, sin lógica ni metadata técnica.
3. `Owner.create()` recibe el agregado `User` completo (no `long userId`) para garantizar integridad referencial.

---

## 2. Ordeñar en el caso de uso

```java
// DESPUÉS de transaction.write().build() exitosa — NUNCA dentro
.flatMap(v -> {
    user.pullDomainEvents().forEach(eventPublisher::send);
    return Result.success(response);
});
```

- Si la transacción falla, los eventos nunca se publican.
- El riesgo de "olvidar ordeñar" se mitiga triple: tests obligatorios + code review + skill `domain-events`. AOP fue evaluado y descartado ([ADR-0022](../../architecture/adr/adr-0022-eventos-dominio-pull-domain-events.md)).

---

## 3. Naming externo — solo infraestructura

"El dominio produce hechos, la infraestructura los nombra."

- `EventEnvelope` `{type, payload, occurredAt}` — envoltura de transporte.
- `EventNameMapping` — mapa clase→nombre (`OwnerCreated` → `owner.created`), ubicado en `infrastructure/messaging`.
- Cambiar `owner_created_v2` no toca una línea de dominio.

Topics según convención FQCN ([ADR-0032](../../architecture/adr/adr-0032-nomenclatura-fqcn-topics-consumer-groups.md)): `com.jobby.<servicio>.<acción>`.

---

## 4. Publicación a Kafka

- Puerto `DomainEventPublisher`; implementación `EventPublisherAdapter` (no usa nada específico de Kafka: orquesta EventRegistry + TransformationRegistry + MessagingPublisher).
- Metadatos como headers: `eventId`, `occurredOn`, `aggregateType`, `operationType` (`aggregateId` viaja como key del mensaje).
- Reintentos: `@RetryableTopic` con 4 intentos, backoff exponencial (2s × 2.0) + DLT `.DLT`.
- Transporte intercambiable: Kafka ↔ Spring Events locales vía `@Profile`/`@ConditionalOnProperty`.

Ver detalles técnicos en la [guía de mensajería](messaging-guide.md).

---

## 5. Consumir eventos

Consumidor genérico que delega por tipo:

```java
@KafkaListener(topics = "com.jobby.user.create-user", groupId = "com.jobby.user-service.create-user-consumer")
public void receive(...) {
    // envelope → TransformationRegistry (Avro → evento aplicación) → eventBus.dispatch(event)
}
```

- Handlers específicos (`OwnerCreatedEmailHandler`) registrados en `EventHandlerConfig`.
- Logging con `EventLogger` que pone `eventType` en MDC.
- Idempotencia: tabla eventId+estado para handlers críticos; eventos naturalmente idempotentes (emails) sin tabla ([ADR-0024](../../architecture/adr/adr-0024-outbox-pattern-atomicidad-bd-kafka.md)).

---

## 6. Tests obligatorios

Todo caso de uso que muta agregados y emite eventos debe verificar:

1. `create()` emite el evento esperado.
2. `reconstruct()` NO emite eventos.
3. La publicación ocurre solo tras transacción exitosa.
4. Ante fallo de transacción, no hay publicación.

Utilidades: `TestDomainEventCollector` con `@Primary` reemplaza el collector en tests de integración cuando aplique.

---

## Alternativa documentada: collector request-scoped

Existe una variante automática (`RequestDomainEventCollector` `@RequestScope` con publicación en `@PreDestroy` y variante `markSuccess()`). Está documentada como alternativa elegible para equipos que prioricen "no pensar en eventos", pero el proyecto adoptó el pull explícito por *explicitness over magic* ([ADR-0039](../../architecture/adr/adr-0039-principio-explicitness-over-magic.md)).

---

## Checklist

- [ ] ¿El evento nace en el método de negocio del agregado?
- [ ] ¿Es record con primitivos/String?
- [ ] ¿El use case ordeña después de la transacción?
- [ ] ¿El naming externo está solo en `EventNameMapping`?
- [ ] ¿Los 4 tests obligatorios existen?
