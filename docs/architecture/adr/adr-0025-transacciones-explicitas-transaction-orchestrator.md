# ADR-0025: Transacciones explícitas con TransactionOrchestrator

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

Los casos de uso que modifican múltiples agregados (ej. crear User + Owner en una sola operación de negocio) necesitan atomicidad. El mecanismo declarativo por defecto (`@Transactional` sobre el método del caso de uso) tiene dos problemas en esta arquitectura: oculta el alcance transaccional (violando *explicitness over magic*) y no compone bien con el estilo Result/flatMap donde las tareas de persistencia se preparan antes de ejecutarse.

## Impulsores de la decisión

* Alcance transaccional visible en el código del caso de uso.
* Composición: preparar N tareas, ejecutarlas atómicamente, con rollback explícito si alguna falla.
* Desacoplamiento del mecanismo de Spring (testable sin contexto).
* Eventos jamás dentro de la transacción ([ADR-0022](adr-0022-eventos-dominio-pull-domain-events.md)).

## Opciones consideradas

### Opción A: `@Transactional` declarativo en todos los casos de uso

* Bien, porque una anotación y listo.
* Malo, porque alcance invisible; rollback implícito solo vía excepciones (choca con Zero Exceptions [ADR-0016](adr-0016-result-pattern-unico.md)); difícil de testear sin proxy.

### Opción B: Transacciones manuales con `TransactionTemplate` dispersas

* Bien, porque control explícito.
* Malo, porque boilerplate repetido; cada caso de uso reinventa su orquestación.

### Opción C: TransactionOrchestrator con patrón prepare/commit (Unit of Work diferido)

Los repositorios **no guardan**: devuelven `Result<PersistenceTask, Error>` — una lambda diferida que sabe persistir el agregado. El caso de uso compone las tareas y confirma:

```java
prepareSave(user)
    .flatMap(userTask -> prepareSave(owner).map(ownerTask -> List.of(userTask, ownerTask)))
    .flatMap(tasks -> transaction.write()
        .add(tasks.get(0))
        .add(tasks.get(1))
        .build())          // ejecuta TODO dentro de UNA transacción
    .map(v -> publishAndRespond());
```

Rollback explícito: si algún `Result` interno es failure, `TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()` dentro del `@Transactional` del orquestador. Variante `read()` con `@Transactional(readOnly=true)`.

* Bien, porque el alcance transaccional se lee literalmente en el pipeline; preparación sin efectos hasta el commit; soporta sesión MongoDB vía `SpringDataTransactionalContext`.
* Malo, porque concepto propio (`PersistenceTask`) que aprender; los repositorios devuelven tareas en vez de resultados guardados (cambio mental para newcomers).

## Decisión

Opción elegida: **C**. Regla complementaria: `@Transactional` declarativo permitido **solo** en handlers de eventos o procesos batch, donde no se requiere control fino; operaciones multi-agregado exigen siempre el orquestador. Y la regla transversal: ningún efecto secundario (publicación de eventos) dentro de la transacción.

### Justificación

El patrón prepare/commit convierte la transacción en un valor componible más del pipeline Result — coherente con todo el estilo del codebase — mientras mantiene la garantía de atomicidad real de Spring Data. Es Unit of Work explícito sin magia.

## Consecuencias

* **Positivas**: atomicidad multi-agregado legible; rollback determinista ante failures; tests de casos de uso sin BD real (mockeando tareas).
* **Negativas**: vocabulario propio documentado en [guía de arquitectura](../../internal/guides/architecture-rules.md); skill `transaction-consistency` planificado ([ADR-0038](adr-0038-skills-opencode-guardianes.md)).
* **Neutras**: traducción sistemática de excepciones DAO → ErrorType ocurre en el proxy de persistencia subyacente ([ADR-0011](adr-0011-mongodb-json-schema-indices-hmac.md)).

## Validación

Tests de casos de uso multi-agregado verifican commit y rollback; skill planificado verifica que eventos nunca se publican dentro de transacciones.

## Referencias

* [ADR-0022](adr-0022-eventos-dominio-pull-domain-events.md) · [ADR-0016](adr-0016-result-pattern-unico.md)
