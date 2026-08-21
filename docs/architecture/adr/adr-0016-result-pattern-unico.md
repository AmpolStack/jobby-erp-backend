# ADR-0016: Result Pattern como mecanismo único de flujo de negocio

* Fecha: 2026-08-20 (retro-documentada)
* Estado: aceptada

## Contexto y problema

En Java, la vía convencional para comunicar fallos de negocio es lanzar excepciones. Esto tiene tres problemas estructurales para un dominio fiscal: los fallos esperables (email duplicado, identificación inválida) no son *excepcionales* sino parte normal del flujo; las excepciones son invisibles en las firmas de los métodos (nadie sabe qué puede fallar sin leer la implementación); y el manejo termina disperso en `try/catch` anidados que nadie puede razonar de punta a punta.

## Impulsores de la decisión

* Explicitidad total: la firma debe declarar todos los resultados posibles.
* Composición funcional: encadenar operaciones con fallo temprano sin pirámides de `if`.
* Distinción nítida entre error de negocio (esperable) y bug/fallo técnico (excepcional).
* Testabilidad: aserciones sobre estados, no sobre excepciones lanzadas.

## Opciones consideradas

### Opción A: Excepciones checked para reglas de negocio

* Bien, porque el compilador obliga a declararlas.
* Malo, porque contaminan todas las firmas intermedias, se envuelven/re-envuelven sin sentido y conviven mal con streams/functional composition.

### Opción B: Excepciones runtime + `@ControllerAdvice` global

* Bien, porque código "limpio" sin manejo visible.
* Malo, porque exactamente el problema descrito: flujo invisible, errores de negocio indistinguibles de bugs, y catch-all que traga contexto.

### Opción C: Result Pattern con sealed interface

`Result<T, Error>` como sealed interface con records `Success<T>` / `Failure<Error>`, API funcional completa (`map`, `flatMap`, `peek`, `fold`), fábricas `success()`/`failure(...)`. Regla **"Zero Exceptions for Business Rules"**: prohibido lanzar excepciones por reglas de negocio; única excepción permitida `InconsistencyResultException`, que representa exclusivamente bugs de programación (estado imposible del propio Result).

* Bien, porque el flujo completo es legible en la firma; composición flatMap plana y verificable; errores tipados por `ErrorType` ([ADR-0018](adr-0018-taxonomia-errores-tres-niveles.md)); aserciones dedicadas ([ADR-0036](adr-0036-stack-testing-resultassertions-test-jar.md)).
* Malo, porque verbosidad inicial y curva para desarrolladores acostumbrados a excepciones; riesgo de ignorar `Failure` si nadie fuerza su inspección (mitigado por convención y review).

## Decisión

Opción elegida: **C**. El patrón es inviolable y transversal:

```java
// Caso de uso canónico: pipeline plano, cero throw
prepareSave(user)
    .flatMap(userTask -> prepareSave(owner).map(ownerTask -> List.of(userTask, ownerTask)))
    .flatMap(tasks -> transaction.write().add(tasks.get(0)).add(tasks.get(1)).build())
    .map(v -> publishEventAndReturn(response));
```

Alcance: puertos, casos de uso, VOs, agregados, validadores, adapters — todo retorna `Result`. Las excepciones quedan reservadas para infraestructura (donde un adapter traduce excepciones externas a `ErrorType` antes de propagar, ej. [PersistenceProxy](../../internal/guides/architecture-rules.md)).

### Justificación

Para un sistema cuyo producto es confianza (facturas fiscales), el flujo de errores es tan importante como el feliz — merece ser explícito y compilable. La experiencia del codebase confirma que los pipelines flatMap son auto-documentados y trivialmente testeables.

## Consecuencias

* **Positivas**: firmas completas; composición plana; errores tipados end-to-end; base para Problem Details sanitizado ([ADR-0030](adr-0030-sanitizacion-errores-support-id.md)).
* **Negativas**: ceremonia adicional; requiere disciplina para no mezclar estilos (review lo vigila).
* **Neutras**: `mapError` marcado `Legacy` en el código — candidato a eliminación ([backlog](../../internal/backlog.md)).

## Validación

Skill automatizado `result-pattern-usage` planificado ([ADR-0038](adr-0038-skills-opencode-guardianes.md)); code review rechaza `throw` de reglas de negocio; `ResultAssertions` hace mecánica la verificación en tests.

## Referencias

* [Patrón público: Result Pattern](../patterns/result-pattern.md)
* [ADR-0017](adr-0017-validationchain-perezosa.md) · [ADR-0018](adr-0018-taxonomia-errores-tres-niveles.md) · [ADR-0027](adr-0027-validacion-distribuida-por-capas.md)
