# ADR-0021: Contratos como records inmutables en `application/`

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

Los contratos del sistema — Commands, Queries, Events y Responses — son datos que cruzan capas. Su ubicación original era `domain/contract/`, pero el dominio quedó contaminado con records anémicos de transporte que no expresaban lenguaje de negocio: el centro puro ([ADR-0003](adr-0003-ddd-hexagonal-dominio-puro.md)) contenía plomería.

Un refactor posterior (documentado en `refactor-contracts-application`, hoy absorbido aquí) reubicó los contratos y eliminó interfaces de use case sin consumidores. Este ADR formaliza el estado final.

## Impulsores de la decisión

* Dominio puro: solo VOs, agregados, puertos y lógica — cero DTOs de transporte.
* Contratos inmutables por defecto: los datos que cruzan límites no mutan.
* Catálogo cerrado: conocer la lista completa de commands/queries en compilación.
* Eliminar abstracciones sin consumidores (YAGNI aplicado).

## Opciones consideradas

### Opción A: Contratos en `domain/` (estado original)

* Bien, porque "todo lo conceptual" en dominio.
* Malo, porque records de transporte no son modelo de negocio; contaminan el centro puro; acoplan dominio a necesidades HTTP/mensajería.

### Opción B: Clases mutable con getters/setters

* Bien, porque familiar para frameworks.
* Malo, porque estado compartido mutable entre capas; construcción parcial posible; thread-safety comprometida.

### Opción C: Records inmutables con sealed markers en `application/contracts/`

```java
public sealed interface Command permits CreateUserCommand, ... {}
public record CreateUserCommand(int identificationTypeId, String firstName, ...)
        implements Command {}
```

Estructura: `application/contracts/{commands,queries,events,responses}`. Los adapters de use case implementan `CommandHandler<Cmd, Resp>` / `QueryHandler` / `EventHandler` de `shared`.

* Bien, porque inmutabilidad garantizada por el lenguaje; sealed interface = catálogo exhaustivo verificable; dominio limpio.
* Malo, porque mover contratos fuera de dominio rompió referencias (14 archivos migrados — costo puntual ya pagado).

## Decisión

Opción elegida: **C**, con dos decisiones complementarias del mismo refactor:

1. **Eliminar interfaces UseCase sin consumidores** (`CreateOwnerUseCase`, etc. — 6 eliminadas): los adapters van directos al bus. Criterio residual: crear interfaz UseCase solo si algo necesita referenciarla sin pasar por el bus.
2. **Naming de respuestas HTTP**: los DTOs de infraestructura llevan sufijo `HttpResponse` (`UserHttpResponse`) para distinguirlos de los contracts de aplicación — se evaluó eliminar la doble capa y se descartó: acoplaría API pública a contratos internos.

### Justificación

El dominio gana legibilidad y las fronteras quedan semánticamente claras: *application* orquesta y transporta; *domain* modela. Los sealed markers dan al compilador el poder de auditar el catálogo completo de intenciones del sistema.

## Consecuencias

* **Positivas**: dominio puro verificado; catálogo de contratos cerrado; inmutabilidad gratis con records; menos abstracciones muertas.
* **Negativas**: dos vocabularios de respuesta (contract vs HttpResponse) que documentar en [guía de arquitectura](../../internal/guides/architecture-rules.md).
* **Neutras**: los events de aplicación se distinguen de los DomainEvent externos ([ADR-0023](adr-0023-separacion-event-interno-domainevent-externo.md)).

## Validación

Review verifica ubicación de contratos nuevos; skill `use-case-pattern` planificado ([ADR-0038](adr-0038-skills-opencode-guardianes.md)); el catálogo sealed hace visibles los contratos huérfanos.

## Referencias

* [ADR-0006](adr-0006-cqrs-parcial.md) · [ADR-0020](adr-0020-buses-in-process-command-query-event.md) · [ADR-0034](adr-0034-reference-data-cache-in-memory.md)
