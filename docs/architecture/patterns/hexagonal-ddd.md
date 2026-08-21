# Hexagonal + DDD con dominio puro

* Guía educativa · Decisión: [ADR-0003](../adr/adr-0003-ddd-hexagonal-dominio-puro.md) · Reglas operativas: [guía interna](../../internal/guides/architecture-rules.md)

---

## El problema

En la arquitectura en capas clásica (controller → service → repository), la lógica de negocio se filtra hacia los bordes: el "servicio" termina siendo una clase de 800 líneas que conoce HTTP, JPA y las reglas del negocio al mismo tiempo. Cambiar la base de datos rompe el negocio; cambiar una regla fiscal toca infraestructura. Y probar el negocio exige levantar medio framework.

Para un dominio como la facturación colombiana — donde las reglas cambian por normativa (DIAN) y no por capricho técnico — esa mezcla es inviable.

## La idea central

Dos movimientos complementarios:

**1. Hexagonal (puertos y adaptadores):** el núcleo define *interfaces* de lo que necesita (puertos de salida) y de lo que ofrece (puertos de entrada); la infraestructura implementa/adapta. Las dependencias apuntan **solo hacia adentro**.

**2. DDD táctico:** dentro del núcleo, el modelo habla el lenguaje del negocio — agregados con invariantes (`Owner`, `User`), Value Objects autovalidados (`Email`, `IdentificationNumber`), eventos de dominio (`OwnerCreated`).

```
                    ┌──────────────────────────────────┐
   REST ──────────► │ application/useCases             │
   Kafka ◄────────► │        │ depende de ▼            │
   MongoDB ◄─────── │ domain/ports/in          domain/ │
   Redis ◄────────► │        │                  models/│
   S3 ◄───────────► │ domain/ports/out ◄─ implementa ─┤─ infrastructure/adapters
                    └──────────────────────────────────┘
```

## Cómo se ve en este codebase

La regla más importante es verificable con un grep: **`domain/` tiene cero imports de Spring o infraestructura**.

```java
// domain/models/vo/shared/Email.java — un VO típico
public static Result<Email, Error> of(String value, String fieldName) {
    // ValidationChain valida formato → nunca existe un Email inválido
}

// domain/ports/out/repositories/UserRepository.java — puerto puro
public interface UserRepository {
    Result<Boolean, Error> existByEmail(Email email);   // sin anotaciones
}
```

El caso de uso orquesta sin conocer detalles técnicos:

```java
// application/useCases/CreateOwnerUseCaseAdapter.java (simplificado)
prepareSave(user)
    .flatMap(userTask -> prepareSave(owner).map(...))
    .flatMap(tasks -> transaction.write().add(userTask).add(ownerTask).build())
    .map(v -> publishEventsAndRespond());
```

## Trade-offs honestos

| A favor | En contra |
|---|---|
| Dominio testeable sin framework ni BD | Más archivos por feature (puerto + adapter + contratos) |
| Reglas fiscales aisladas de cambios técnicos | Curva inicial para quienes vienen de capas clásicas |
| Fronteras auditables (grep-ables) | Riesgo de anemia si los agregados no absorben comportamiento |

**Cuándo NO usarlo:** CRUDs puros sin reglas de negocio, prototipos desechables. El patrón paga cuando el dominio es rico — y facturación lo es.

## Dónde verlo

- Reglas completas: [`docs/internal/guides/architecture-rules.md`](../../internal/guides/architecture-rules.md)
- Ejemplo canónico: `jobby/user-service/src/main/java/com/jobby/userservice/{domain,application,infrastructure}`
- La deuda conocida de anemia está registrada en el [backlog](../../internal/backlog.md) — transparencia incluida.

## Para ir más profundo

- Evans, E. — *Domain-Driven Design* (el libro fundacional)
- Vernon, V. — *Implementing Domain-Driven Design*
- Cockburn, A. — *Hexagonal Architecture* (artículo original)
