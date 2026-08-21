# ADR-0022: Eventos de dominio vía `pullDomainEvents()`

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

El sistema originalmente notificaba efectos secundarios (ej. email de bienvenida) mediante un puerto `EmailSenderService` con un método por tipo de email — cada nuevo efecto exigía modificar el puerto y el caso de uso, violando OCP. La solución natural son eventos de dominio: el agregado expresa el hecho (`OwnerCreated`) y handlers de infraestructura reaccionan.

Quedaba la pregunta de diseño más importante: **¿quién crea, acumula y publica los eventos?** Se analizaron tres estrategias en profundidad (documentos `domain-events-design`, `domain-events-collector-implementation` y `domain-events-pull-events-implementation`, hoy absorbidos aquí).

## Impulsores de la decisión

* El dominio expresa hechos de negocio; la infraestructura decide qué hacer con ellos.
* Atomicidad semántica: si la transacción falla, los eventos nunca deben publicarse.
* Dominio puro: sin dependencias técnicas para emitir eventos ([ADR-0003](adr-0003-ddd-hexagonal-dominio-puro.md)).
* Explicitness: el flujo del evento debe ser visible leyendo el caso de uso.

## Opciones consideradas

### Opción A — Estrategia 3B: el caso de uso crea el evento explícitamente

El agregado no sabe de eventos; tras la transacción, el use case hace `publish(new OwnerCreated(...))`.

* Bien, porque máxima simplicidad inicial; agregado 100% libre de eventos.
* Malo, porque el conocimiento del "hecho ocurrido" vive fuera del agregado que lo protagonizó; riesgo de divergencia entre estado y hechos declarados.

### Opción B — Estrategia collector request-scoped: publicación automática

Un `RequestDomainEventCollector` (`@RequestScope`) acumula eventos y los publica automáticamente al cerrar el request (`@PreDestroy`), con variante `markSuccess()` para no publicar ante error.

* Bien, porque "no pensar en eventos": cero código de publicación por caso de uso.
* Malo, porque la publicación implícita viola explicitness; el ciclo de vida oculto complica tests (requiere reemplazar el collector con `@Primary`) y puede publicar en flujos inesperados.

### Opción C — Estrategia 3A: `pullDomainEvents()` (DDD puro, Evans/Vernon)

El agregado acumula eventos internamente en una lista final sin getter público; tras la transacción exitosa, el caso de uso los "ordeña":

```java
// dentro del aggregate
private final List<DomainEvent> domainEvents = new ArrayList<>(); // @Getter(NONE)

// en el use case, DESPUÉS de transaction.write().build() exitosa
user.pullDomainEvents().forEach(publisher::send);
```

* Bien, porque el hecho nace donde ocurre; publicación condicionada explícitamente al éxito de la transacción; si falla, los eventos mueren con la transacción; cero magia.
* Malo, porque exige disciplina: olvidar ordeñar = evento perdido silenciosamente (mitigado: tests + review + skill).

### Opción D — Estrategia 3C: inyectar `DomainEventPublisher` como parámetro del aggregate — DESCARTADA

* Bien, porque publicación inmediata desde el aggregate.
* Malo, porque introduce acoplamiento técnico en el dominio sin beneficio real frente a 3A/3C. Descartada explícitamente.

## Decisión

Opción elegida: **C — `pullDomainEvents()`**, como evolución natural de la estrategia 3B inicial. Reglas fijas:

1. El agregado acumula eventos en métodos de negocio (`create`, `updateEmail`, ...).
2. `reconstruct()` **jamás** emite eventos ([ADR-0019](adr-0019-fabrica-doble-of-reconstruct.md)).
3. El ordeñe ocurre solo después de transacción exitosa — nunca dentro ([ADR-0025](adr-0025-transacciones-explicitas-transaction-orchestrator.md)).
4. Los eventos son records con primitivos/String únicamente (serializables, sin lógica).
5. Mitigación del riesgo de olvido: tests que verifican publicación post-transacción y ausencia ante fallo + code review + skill `domain-events`. AOP (`@AfterReturning`) fue evaluado y descartado: complejo y frágil.

### Justificación

Es la estrategia canónica de DDD táctico: equilibra pureza del dominio, atomicidad semántica y explicitud total. Las alternativas fueron implementadas/documentadas antes de decidir — la elección está basada en evidencia del propio codebase, no en teoría.

## Consecuencias

* **Positivas**: OCP restaurado (nuevos efectos = nuevos handlers, sin tocar puertos); eventos imposibles de publicar sobre transacciones fallidas; dominio expresivo.
* **Negativas**: riesgo residual de ordeñe olvidado (mitigado triple); lista de eventos en memoria hasta el ordeñe.
* **Neutras**: naming externo del evento es asunto de infraestructura ([ADR-0023](adr-0023-separacion-event-interno-domainevent-externo.md)).

## Validación

Convención de tests obligatoria por caso de uso con eventos; skill `domain-events` planificado ([ADR-0038](adr-0038-skills-opencode-guardianes.md)); [patrón público](../patterns/domain-events.md) documenta el flujo completo.

## Referencias

* [Patrón público: Domain Events](../patterns/domain-events.md)
* Vernon, V. *Implementing Domain-Driven Design* — patrón de eventos del agregado
