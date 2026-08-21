# Result Pattern

* Guía educativa · Decisión: [ADR-0016](../adr/adr-0016-result-pattern-unico.md) · Complementos: [ADR-0017](../adr/adr-0017-validationchain-perezosa.md), [ADR-0018](../adr/adr-0018-taxonomia-errores-tres-niveles.md)

---

## El problema

En Java, comunicar un fallo de negocio (email duplicado, identificación inválida) con excepciones tiene tres costos ocultos:

1. **Invisibilidad**: la firma `User updateEmail(String email)` no dice que puede fallar ni cómo.
2. **Confusión de categorías**: "email duplicado" no es *excepcional* — es martes normal en una app de facturación. Tratarlo como excepción mezcla flujo esperado con bugs.
3. **Manejo disperso**: cada capa decide si atrapa, envuelve o ignora — nadie puede razonar el flujo completo.

## La idea central

Los fallos esperables son **valores de retorno**, no interrupciones:

```java
public sealed interface Result<T, E> {
    record Success<T, E>(T data) implements Result<T, E> {}
    record Failure<T, E>(Error error) implements Result<T, E> {}
}
```

Con API funcional para componer sin pirámides:

```java
// El flujo completo es legible: cada paso declara qué puede fallar
ValidationChain.create()
    .validateNotBlank(rawEmail, fieldName)
    .build()
    .flatMap(v -> Email.of(rawEmail, fieldName))       // Result<Email, Error>
    .flatMap(email -> userRepository.existByEmail(email)
        .filter(exists -> !exists)                      // unicidad
        .map(v -> new ChangeEmailCommand(user, email)));
```

La regla cultural que lo sostiene: **Zero Exceptions for Business Rules**. Las excepciones quedan para bugs (`InconsistencyResultException`) y fallos de infraestructura — donde sí son excepcionales.

## Lo que lo hace potente en este codebase

El Result no viaja solo; se conecta con dos piezas:

- **ValidationChain** ([guía interna](../../internal/guides/validation-guide.md)): validaciones declaradas como cadena perezosa con cortocircuito — la parte más mecánica del código se vuelve la más legible.
- **Taxonomía ErrorType** ([ADR-0018](../adr/adr-0018-taxonomia-errores-tres-niveles.md)): el tipo de error codifica su audiencia (usuario / interno / sistema) y la sanitización hacia HTTP es automática.

## Trade-offs honestos

| A favor | En contra |
|---|---|
| Firmas completas: imposible olvidar un caso de fallo | Verbosidad inicial y curva de composición |
| Pipelines planos, testables con aserciones dedicadas | Riesgo de ignorar `Failure` si nadie fuerza inspección |
| Errores tipados end-to-end hasta Problem Details | Estilo poco familiar para devs de excepciones |

**Cuándo NO usarlo:** scripts cortos, prototipos, o donde el ecosistema ya impone excepciones (configuración Spring al startup — ahí fail-fast con excepción es correcto).

## Dónde verlo

- Implementación: `jobby/shared/src/main/java/com/jobby/domain/mobility/result/`
- Uso canónico: cualquier `*UseCaseAdapter` en `user-service`
- Aserciones de test: `ResultAssertions` en el test-jar de shared
- Reglas operativas: [`docs/internal/guides/validation-guide.md`](../../internal/guides/validation-guide.md)

## Para ir más profundo

- Scott Wlaschin — *Railway Oriented Programming* (la idea original en F#, aplica directo)
- [ADR-0025](../adr/adr-0025-transacciones-explicitas-transaction-orchestrator.md) — cómo las transacciones se vuelven valores componibles del mismo pipeline
