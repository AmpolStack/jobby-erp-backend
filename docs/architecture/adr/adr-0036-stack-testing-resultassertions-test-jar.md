# ADR-0036: Stack de testing con ResultAssertions distribuido vía test-jar

* Fecha: 2026-08-20 (retro-documentada)
* Estado: aceptada

## Contexto y problema

Con Result Pattern ([ADR-0016](adr-0016-result-pattern-unico.md)), cada test necesita asertar sobre `Result<T, Error>`: éxito con valor esperado, fallo con tipo exacto, fallo con error completo. Sin utilidades dedicadas, cada test reinventa sus aserciones — inconsistencia garantizada. Además, las utilidades deben estar disponibles para **todos** los servicios del monorepo, no solo para `shared`.

## Impulsores de la decisión

* Aserciones idiomáticas para el patrón central del sistema.
* Distribución de utilidades de test entre módulos sin exponerlas en producción.
* Stack estándar JVM, sin frameworks exóticos.
* Datos de prueba de frontera reutilizables (emails válidos/inválidos, nulls, blanks).

## Opciones consideradas

### Opción A: Aserciones inline en cada test (`assertTrue(result.isSuccess())`)

* Bien, porque cero infraestructura.
* Malo, porque mensajes de fallo inútiles; duplicación masiva; verificación de ErrorType dispersa e inconsistente.

### Opción B: Librería propia publicada como artefacto separado

* Bien, porque versionamiento independiente.
* Malo, porque overhead de release coordinado que el monorepo elimina justamente ([ADR-0004](adr-0004-monorepo-maven-multi-modulo.md)).

### Opción C: JUnit 5 + AssertJ + Mockito + utilidades propias en test-jar compartido

Stack base: JUnit Jupiter 5.11 + AssertJ 3.27 + Mockito 5. Encima:

- **`ResultAssertions`**: `assertSuccess`, `assertFailure`, `assertFailure(result, errorType)` (verifica código exacto con `assertSame`), `assertFailure(result, expectedResult)`.
- **Paquete `boundaries`**: proveedores de datos de frontera como `Stream<Arguments>` para `@ParameterizedTest` (`EmailBoundaries`, `PhoneBoundaries`, `NullityBoundaries`, ...).
- Distribución: `maven-jar-plugin` con goal `test-jar` en `shared`; los servicios dependen de `shared` type `test-jar` scope `test`.

* Bien, porque aserciones expresivas con mensajes claros; una sola fuente de utilidades; scope test garantiza cero fuga a producción; AssertJ cubre lo que ResultAssertions no.
* Malo, porque mecanismo test-jar menos conocido que una librería normal (documentado aquí); mockito-inline legacy coexistiendo con Mockito 5 (deuda en [backlog](../../internal/backlog.md)).

## Decisión

Opción elegida: **C**. Regla de uso: AssertJ directo solo para aserciones que `ResultAssertions` no cubre; toda aserción sobre Result pasa por las utilidades.

### Justificación

El test-jar es el mecanismo Maven nativo para compartir test utilities dentro de un multi-módulo — cero infraestructura de publicación, versionado unificado por el monorepo. Las utilidades convierten el estilo Result en algo tan fácil de asertar que no existe excusa para tests flojos.

## Consecuencias

* **Positivas**: consistencia total de aserciones; onboarding rápido (las utilidades se auto-explican); datos de frontera centralizados y curados.
* **Negativas**: deuda puntual de Mockito dual (registrada); acoplamiento de tests a utilidades compartidas (deseado).
* **Neutras**: la pirámide de testing 60/25/15 se define en [ADR-0037](adr-0037-convenciones-tests.md).

## Validación

Review rechaza aserciones inline sobre Result; los boundaries crecen conforme aparecen nuevos tipos de dato.

## Referencias

* [Guía interna de testing](../../internal/guides/testing-conventions.md)
