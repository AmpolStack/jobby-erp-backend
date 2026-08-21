# Domain Events

* Guía educativa · Decisiones: [ADR-0022](../adr/adr-0022-eventos-dominio-pull-domain-events.md), [ADR-0023](../adr/adr-0023-separacion-event-interno-domainevent-externo.md) · Guía operativa: [internal/guides/domain-events-guide.md](../../internal/guides/domain-events-guide.md)

---

## El problema

Los efectos secundarios son la parte más frágil de cualquier sistema: "cuando se crea un owner, enviar email de bienvenida". La solución ingenua — un `EmailSenderService` con un método por tipo de email llamado desde el caso de uso — escala mal: cada nuevo efecto modifica el puerto y el caso de uso (violación de OCP), y el caso de uso termina orquestando una lista creciente de efectos ajenos a su responsabilidad.

Peor aún: ¿qué pasa si el email se envía *antes* de que la transacción confirme? El usuario recibe bienvenida por un owner que nunca existió.

## La idea central

Invertir el flujo: el agregado **declara el hecho** (`OwnerCreated`), y quien quiera reaccionar, reacciona. El caso de uso deja de conocer efectos; solo publica hechos.

La pregunta de diseño es *quién acumula y cuándo publica*. Este proyecto evolucionó por tres estrategias hasta llegar a la canónica de DDD:

```java
// 1. El agregado acumula el hecho donde ocurre
public static Result<Owner, Error> create(User user, ...) {
    var owner = new Owner(...);
    owner.domainEvents.add(new OwnerCreated(...));   // lista interna sin getter
    return Result.success(owner);
}

// 2. El caso de uso lo "ordeña" DESPUÉS de la transacción exitosa
transaction.write().add(userTask).add(ownerTask).build()
    .map(v -> {
        owner.pullDomainEvents().forEach(eventPublisher::send);  // ← solo si commit OK
        return response;
    });
```

Si la transacción falla, los eventos mueren con ella. **Es imposible publicar un hecho sobre un estado que no ocurrió.**

## Las reglas que lo hacen seguro

1. **`reconstruct()` jamás emite eventos** — hidratar desde BD no es acción de negocio. Sin esta regla, cada lectura del owner regeneraría su email de bienvenida.
2. **Dos tipos de evento**: `Event` in-process (N handlers en la JVM) vs `DomainEvent` externo (viaja por Kafka). El dominio produce hechos; la infraestructura decide naming y transporte ([ADR-0023](../adr/adr-0023-separacion-event-interno-domainevent-externo.md)).
3. **Naming exclusivo de infraestructura**: cambiar `owner_created` → `owner.created.v2` no toca una línea de dominio.

## Por qué no las alternativas

| Alternativa | Por qué fue descartada |
|---|---|
| Collector request-scoped automático | Publicación implícita al cerrar el request: mágica, difícil de testear |
| Inyectar publisher al agregado | Acopla el dominio a infraestructura técnica |
| AOP `@AfterReturning` para ordeñar | Complejo y frágil — el corte no sabe qué transacción falló |

El riesgo residual del pull explícito ("olvidar ordeñar") se mitiga triple: tests obligatorios + code review + skill automatizado.

## Trade-offs honestos

| A favor | En contra |
|---|---|
| OCP real: nuevos efectos = nuevos handlers | Riesgo de ordeñe olvidado (mitigado triple) |
| Atomicidad semántica garantizada | Lista de eventos en memoria hasta el ordeñe |
| Dominio expresivo: los hechos viven donde ocurren | Tres representaciones por flujo (dominio → envelope → Avro) |

## Dónde verlo

- Agregados con eventos: `user-service/domain/models/aggregate/{User,Owner}.java`
- Ordeñe: cualquier use case con eventos (`CreateOwnerUseCaseAdapter`)
- Naming/envelope: `infrastructure/messaging/`
- Los 4 tests obligatorios: [guía interna](../../internal/guides/domain-events-guide.md)

## Para ir más profundo

- Vernon, V. — *Implementing Domain-Driven Design*, capítulo de eventos del agregado
- [Outbox Pattern](outbox-pattern.md) — el siguiente paso cuando la pérdida de eventos es inaceptable
