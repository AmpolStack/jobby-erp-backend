# Skills de OpenCode — Catálogo

* Tipo: guía interna · Audiencia: contribuyentes que usan OpenCode · Última actualización: 2026-08-20
* Decisión subyacente: [ADR-0038](../../architecture/adr/adr-0038-skills-opencode-guardianes.md)

Este documento lista los skills que deben crearse como archivos `.opencode/skills/<name>/SKILL.md` para que OpenCode los consuma. **Estado: especificados, pendientes de creación** — es tarea priorizada del [backlog](../backlog.md).

Cada skill referencia su documento fuente; el skill verifica, el documento explica.

---

## 1. `ddd-hexagonal-check`

Verificar que los cambios respeten la arquitectura hexagonal y DDD ([guía de arquitectura](architecture-rules.md)):

- `domain/` no debe importar nada de `infrastructure/`
- Los puertos (`ports/in/`, `ports/out/`) deben ser interfaces
- Las implementaciones van en `infrastructure/adapters/`
- Los agregados solo exponen métodos que preservan invariantes
- Las dependencias apuntan hacia adentro (regla de dependencia)

## 2. `aggregate-invariants`

Velar por que los agregados (`Owner`, `Employee`, `User`) mantengan sus invariantes:

- Solo métodos que garanticen estado válido
- Entidades hijas solo se modifican a través del agregado raíz
- Validaciones en constructores/factory methods
- Uso de `ValidationChain` para validaciones

## 3. `value-object-immutabilidad`

Asegurar que los Value Objects (`Email`, `Name`, `Phone`, `IdentificationNumber`, `ImageUrl`, `ContactValue`) sean:

- Inmutables (sin setters, final)
- Validados en construcción (nunca estado inválido)
- `equals()` y `hashCode()` basados en todos sus atributos
- Preferir `record` cuando no haya lógica compleja

## 4. `use-case-pattern`

Verificar que los casos de uso sigan el patrón establecido:

- Contrato en `application/contracts/` + adapter en `application/useCases/*UseCaseAdapter`
- Implementación de `CommandHandler`/`QueryHandler`/`EventHandler` de shared
- Dependencia solo de puertos de salida (no de implementaciones)
- Retorno usando `Result<Success, Failure>`
- Orquestación: validar → preparar → transaccionar → mapear

## 5. `result-pattern-usage`

- Ningún `throw` por reglas de negocio (única excepción: `InconsistencyResultException`)
- Composición con `map`/`flatMap`, pipelines planos
- ValidationChain con la familia correcta (`validate*` vs `validateInternal*`)
- Propagación de VOs validados (nunca raw strings hacia `reconstruct()`)

## 6. `mapstruct-usage`

- Ubicación correcta: `application/mappers/` (dominio↔responses), `infrastructure/persistence/mappers/` (dominio↔MongoDB), `infrastructure/adapters/in/rest/mappers/` (HTTP↔comandos)
- `@Mapping` explícito si difiere el nombre
- Mapeo manual cuando la lógica no cabe en anotaciones (los "falsos" mappers son `@Component`)
- MapStruct NO se usa para transformaciones dominio↔Avro (esas son `Transformation` con Result)

## 7. `testing-standards`

Verificar las convenciones de la [guía de testing](testing-conventions.md):

- Un `@Nested` por método bajo prueba
- Nomenclatura `methodName_WhenCondition_ShouldExpectedBehavior`
- Aserciones sobre Result vía `ResultAssertions`
- Tests de eventos completos donde aplique

## 8. `security-sensitive-data`

Verificar la [guía de seguridad](security-guide.md):

- Campos sensibles con política correcta (`ProtectedField` vs `IndexedField`)
- Unicidad siempre por HMAC, nunca por dato cifrado
- PII como `bytes` cifrados en esquemas Avro
- Payload protegido triple (transient/@Transient/@JsonIgnore)
- Mapeo HTTP solo vía `SecuredFieldMapper`

## 9. `problem-details-errors`

- Controllers sin excepciones ni Results crudos
- Respuestas vía `HttpResponseProcessor`
- Fallos → Problem Details sanitizado + supportId
- ErrorTypes expuestos registrados en `ErrorTypeHttpCollection`

## 10. `transaction-consistency`

- Eventos publicados DESPUÉS de la transacción, nunca dentro
- Operaciones multi-agregado vía `TransactionOrchestrator` (prepare/commit)
- `@Transactional` declarativo solo en handlers de eventos/batch
- Rollback explícito ante failures dentro del orquestador

## 11. `domain-events`

- Todo caso de uso que modifica agregados llama `pullDomainEvents()`
- El ordeñe ocurre tras transacción exitosa
- `reconstruct()` jamás emite eventos
- Naming externo solo en `EventNameMapping`

---

## Cómo crear un skill

```markdown
# .opencode/skills/<name>/SKILL.md
---
description: <cuándo activarse>
---

<instrucciones de verificación, referenciando la guía fuente>
```

Mantener sincronizado con su guía: si la guía cambia, el skill se actualiza en el mismo PR.
