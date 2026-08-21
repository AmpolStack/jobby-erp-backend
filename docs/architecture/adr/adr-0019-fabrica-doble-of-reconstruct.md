# ADR-0019: Fábrica doble `of()`/`reconstruct()` para VOs y agregados

* Fecha: 2026-08-20 (retro-documentada)
* Estado: aceptada

## Contexto y problema

Los Value Objects y agregados deben garantizar que **nunca existen en estado inválido**. Pero hay dos orígenes de datos radicalmente distintos: entrada de usuario (desconfiada, requiere validación) y base de datos (confiable — el dato ya fue validado cuando se guardó). Validar dos veces lo mismo es desperdicio; no validar la entrada de usuario es un bug.

Además, con eventos de dominio ([ADR-0022](adr-0022-eventos-dominio-pull-domain-events.md)) surgió un requisito crítico: reconstruir desde BD **no** es una acción de negocio y no debe emitir eventos (si `reconstruct()` emitiera `OwnerCreated`, cada lectura regeneraría el email de bienvenida).

## Impulsores de la decisión

* Invariantes garantizadas en construcción: imposible crear estado inválido.
* Cero re-validación redundante al hidratar desde BD.
* Distinción semántica entre "crear" (acción de negocio) y "reconstruir" (hidratación).
* Constructores inaccesibles: la fábrica es la única puerta.

## Opciones consideradas

### Opción A: Constructor público + validación en setter

* Bien, porque familiar.
* Malo, porque permite construir inválido; validación opcional en la práctica; setters rompen inmutabilidad.

### Opción B: Fábrica única que siempre valida

* Bien, porque una sola puerta.
* Malo, porque obliga a validar datos ya confiables en cada hidratación, o a pasar flags booleanos (`validate=false`) que nadie respeta.

### Opción C: Fábrica doble con constructor privado

```java
// 1. Con validación → entrada de usuario; retorna Result
public static Result<Name, Error> of(String value, String fieldName) { ... }

// 2. Sin validación → fuente confiable (BD); nunca emite eventos
public static Name reconstruct(String value) { return new Name(value); }

// Constructor privado vía Lombok
@AllArgsConstructor(access = AccessLevel.PRIVATE)
```

En agregados: `create()` / `reconstruct()` con las mismas semánticas. La lista interna de eventos se oculta con `@Getter(AccessLevel.NONE)`.

* Bien, porque cada origen tiene su puerta explícita; el compilador impide saltarse la validación en creación; `reconstruct()` sin eventos resuelve el problema de duplicados por diseño.
* Malo, porque dos métodos que mantener por tipo; riesgo de usar `reconstruct()` con datos no confiables (vigilado en review).

## Decisión

Opción elegida: **C**, aplicada universalmente a VOs y agregados. Reglas fijas:

1. `of()`/`create()` **siempre** valida vía ValidationChain ([ADR-0017](adr-0017-validationchain-perezosa.md)) y recibe `fieldName` para errores contextualizados.
2. `reconstruct()` **nunca** valida ni emite eventos.
3. Constructor privado; sin setters; campos `final`; colecciones inicializadas vacías e inmutables (`Set.of()`, nunca `null`).
4. Las mutaciones de agregado retornan `Result<Void, Error>` (o `Result<R, Error>` cuando transportan resultado de negocio, ej. `ImageReplaceResult`).

Decisión negativa documentada: **no** crear `UserService` como "solución" a agregados anémicos — un service que envuelva al agregado es anemia disfrazada; con constructor privado, `create()` basta como única entrada.

### Justificación

La fábrica doble codifica la epistemología del dato: *¿de dónde viene?* determina *cuánta desconfianza* merece. Es simple, mecanizable en review y resolvió limpiamente el bug potencial más peligroso del sistema de eventos.

## Consecuencias

* **Positivas**: estados inválidos imposibles; hidratación barata; eventos solo en acciones reales; estilo uniforme en todo el codebase.
* **Negativas**: inconsistencias históricas detectadas y corregidas gradualmente (ej. mutaciones sin Result — deuda en [backlog](../../internal/backlog.md)).
* **Neutras**: Lombok reduce el boilerplate del patrón.

## Validación

Skills `value-object-immutabilidad` y `aggregate-invariants` planificados ([ADR-0038](adr-0038-skills-opencode-guardianes.md)); tests por VO verifican ambas fábricas; convención de tests de eventos: `create()` emite, `reconstruct()` no.

## Referencias

* [Patrón público: Hexagonal + DDD](../patterns/hexagonal-ddd.md) · [ADR-0022](adr-0022-eventos-dominio-pull-domain-events.md)
