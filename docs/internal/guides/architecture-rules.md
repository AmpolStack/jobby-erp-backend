# Guía de Reglas de Arquitectura — DDD + Hexagonal

* Tipo: guía interna (how-to) · Audiencia: contribuyentes · Última actualización: 2026-08-20
* Decisiones subyacentes: [ADR-0003](../../architecture/adr/adr-0003-ddd-hexagonal-dominio-puro.md), [ADR-0005](../../architecture/adr/adr-0005-shared-como-starter-autoconfiguracion.md), [ADR-0019](../../architecture/adr/adr-0019-fabrica-doble-of-reconstruct.md), [ADR-0020](../../architecture/adr/adr-0020-buses-in-process-command-query-event.md), [ADR-0021](../../architecture/adr/adr-0021-contratos-records-application.md)

---

## Aislamiento de capas

```
┌─────────────────────────────────────────────────────────┐
│                    user-service/                          │
│  ┌───────────────────────────────────────────────────┐  │
│  │  application/ (Use Cases)                          │  │
│  │  → depende de domain/ ports/in                     │  │
│  │  → orquesta domain + infrastructure out            │  │
│  │  → contratos en application/contracts/             │  │
│  └───────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────┐  │
│  │  domain/                                           │  │
│  │  → ZERO imports de infrastructure                  │  │
│  │  → ZERO imports de Spring                          │  │
│  │  → Solo usa Result, Error, ValidationChain         │  │
│  │  → Puertos son interfaces puras (sin anotaciones)  │  │
│  └───────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────┐  │
│  │  infrastructure/ (adapters, persistence, rest)     │  │
│  │  → Depende de domain/                              │  │
│  │  → Implementa puertos                              │  │
│  │  → Contiene configuraciones, controladores, BD     │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

**Regla fija:** el paquete `domain/` NO puede importar nada de `infrastructure/`, Spring ni librerías externas (excepto Lombok y `java.*`). Cero excepciones. Violación = deuda técnica automática.

---

## Paquetes dentro de Domain

```
domain/
├── models/
│   ├── aggregate/   → Aggregate Roots
│   ├── enums/       → Enums de dominio
│   └── vo/
│       ├── shared/     → Value Objects persistidos
│       └── ephemeral/  → VOs no persistidos (ej. contenido binario)
├── ports/
│   ├── in/          → Casos de uso (inbound)
│   └── out/         → Repositorios + servicios externos (outbound)
└── mobility/        → Cross-cutting: Result, Error, ValidationChain
```

---

## Value Objects — fábrica doble

Ver decisión completa en [ADR-0019](../../architecture/adr/adr-0019-fabrica-doble-of-reconstruct.md). Resumen operativo:

```java
// 1. Factory CON validación → devuelve Result (entrada de usuario)
public static Result<Name, Error> of(String value, String fieldName) { ... }

// 2. Factory SIN validación → fuente confiable (BD, reconstrucción)
public static Name reconstruct(String value) { return new Name(value); }
```

- Constructor **privado** vía `@AllArgsConstructor(access = AccessLevel.PRIVATE)`.
- `of()` recibe `fieldName` para errores contextualizados.
- Inmutables, campos `final`, sin setters; preferir `record` si no hay lógica compleja.
- Colecciones inicializadas vacías e inmutables (`Set.of()`), nunca `null`.

---

## Aggregate Roots

- Misma fábrica doble: `create()` valida / `reconstruct()` hidrata sin validar **ni emitir eventos**.
- Entidades hijas solo se modifican vía la raíz (`owner.addContact(...)`, nunca tocando la lista).
- Toda mutación retorna `Result<Void, Error>` o `Result<R, Error>` con resultado de negocio (ej. `ImageReplaceResult` transporta la imagen vieja para borrado posterior en S3).
- La lista interna de eventos se oculta con `@Getter(AccessLevel.NONE)`.

**Nota sobre anemia:** los agregados deben absorber comportamiento de negocio. Los casos conocidos de anemia están en el [backlog](../backlog.md). No crear `XxxService` que envuelva al agregado como "solución" — es anemia disfrazada.

---

## Puertos

### Inbound (`ports/in/`)
- Definen casos de uso; retornan `Result<T, Error>`.

### Outbound (`ports/out/`)
- `repositories/` → persistencia (`OwnerRepository`, `UserRepository`).
- `services/` → servicios externos (`EmailSenderService`, `StorageService`, `EncryptionService`, `MacService`, `IdGenerator`, `CacheService`).

**Nota del refactor de contratos:** los Commands/Queries/Events/Responses viven en `application/contracts/`, NO en dominio ([ADR-0021](../../architecture/adr/adr-0021-contratos-records-application.md)). Las interfaces UseCase sin consumidores fueron eliminadas: los adapters van directos al bus.

---

## Reglas de dependencia

```
domain/ports/in   ←  application/useCases  ←  infrastructure/adapters/in/rest
domain/ports/out  →  infrastructure/adapters/out/repositories
domain/ports/out  →  infrastructure/adapters/out/services
shared/domain/ports → shared/infrastructure/adapter/*
```

Ninguna capa interna conoce a la externa. `domain/` es el centro puro.

---

## Buses in-process

Los controllers inyectan **buses**, nunca handlers concretos ([ADR-0020](../../architecture/adr/adr-0020-buses-in-process-command-query-event.md)):

```java
// Controller
validator.validate(request)
    .flatMap(v -> commandBus.dispatch(mapper.toCommand(...)))
    .map(responseProcessor::process);
```

Registro explícito en la AutoConfiguration (cero reflexión):

```java
@Bean
public CommandBus commandBus(List<CommandHandler<?, ?>> handlers, MeterRegistry meterRegistry) {
    // registro encadenado .register(Contrato.class, handler)
    // composición fija: Logging por fuera, Metrics por dentro
}
```

---

## Orquestación estándar del caso de uso

Patrón canónico: **validar → preparar → transaccionar → mapear**

```java
prepareSave(user)                                    // PersistenceTask diferida
    .flatMap(userTask -> prepareSave(owner)
        .map(ownerTask -> List.of(userTask, ownerTask)))
    .flatMap(tasks -> transaction.write()
        .add(tasks.get(0)).add(tasks.get(1)).build()) // UNA transacción
    .map(v -> {
        publishEvents();                              // DESPUÉS del commit
        return response;
    });
```

- El caso de uso nunca genera IDs ni valida campo a campo ([ADR-0027](../../architecture/adr/adr-0027-validacion-distribuida-por-capas.md)).
- Eventos jamás dentro de la transacción ([ADR-0022](../../architecture/adr/adr-0022-eventos-dominio-pull-domain-events.md)).
- Fail-fast: obtener la entidad antes de mutar o transaccionar.

---

## Autoconfiguración Spring (módulo shared)

Cada adapter en `shared/infrastructure/` tiene su propia `@Configuration`:

```java
@Configuration
@ConditionalOnClass(RedisTemplate.class)
public class CacheAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(CacheService.class)  // overridable por el servicio
    public CacheService cacheService(...) { ... }
}
```

Toda nueva pieza transversal entra con: autoconfiguración condicional + entrada en `AutoConfiguration.imports` + metadata de propiedades.

---

## Seguridad (resumen operativo)

Detalle completo en la [guía de seguridad](security-guide.md). Reglas rápidas:

```java
orchestrator.secure()
    .add(protectedField, StorageSecurityPolicy.SECURED_ONLY_ENCRYPTION)
    .add(indexedField, StorageSecurityPolicy.SECURED)
    .executeAll();
```

- `ProtectedField` → solo cifrado (bytes). `IndexedField` → cifrado + HMAC para búsqueda.
- Nunca texto plano para datos sensibles; unicidad siempre por HMAC.

---

## Checklist de review arquitectónico

- [ ] ¿`domain/` libre de imports de infraestructura/Spring?
- [ ] ¿Contratos nuevos en `application/contracts/`?
- [ ] ¿VOs creados con `of()` en mappers (no raw strings hacia `reconstruct()`)?
- [ ] ¿Mutaciones de agregado retornan `Result`?
- [ ] ¿Eventos publicados después de la transacción?
- [ ] ¿Controller inyecta bus, no handler concreto?
- [ ] ¿Campos sensibles con política correcta (Protected vs Indexed)?
