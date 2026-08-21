# ADR-0037: Convenciones de tests — estructura, nomenclatura y alcance

* Fecha: 2026-08-20 (retro-documentada)
* Estado: aceptada

## Contexto y problema

Sin convenciones, cada test file es un país: nombres arbitrarios (`testEncrypt`, `shouldWork`), clases planas imposibles de navegar, tests que prueban getters o el framework mismo. El objetivo educativo del proyecto eleva el estándar: los tests son también documentación ejecutable del comportamiento esperado.

## Impulsores de la decisión

* Tests legibles como especificación: el nombre debe describir escenario y resultado.
* Navegabilidad: encontrar el test de un método en segundos.
* Un escenario por test: fallos precisos, no "test falló por algo entre estas 5 cosas".
* Definir explícitamente qué NO se testea (el costo de tests inútiles es real).

## Opciones consideradas

### Opción A: Convención libre por desarrollador

* Bien, porque cero fricción inicial.
* Malo, porque inconsistencia inmediata; reviews discutiendo estilo en vez de lógica; suite innavegable.

### Opción B: BDD formal (Gherkin/Cucumber) para todo

* Bien, porque legibilidad de negocio máxima.
* Malo, porque capa adicional de abstracción injustificada para tests unitarios de dominio; el Given-When-Then ya se expresa en nomenclatura Java.

### Opción C: Convención estricta documentada

1. **Estructura**: un `@Nested` class por método bajo prueba (`OfMethod`, `CreateMethod`), con `@DisplayName` de clase describiendo el sujeto.
2. **Nomenclatura**: `methodName_WhenCondition_ShouldExpectedBehavior`; `@DisplayName` como frase legible "Given [condición] → Then [resultado]". Prohibidos `testX()`, `shouldWork()`.
3. **Un escenario por test**: variaciones del mismo escenario → `@MethodSource`; rangos válidos → `@ValueSource`; determinismo/aleatoriedad → `@RepeatedTest`.
4. **Qué NO testear**: getters/setters triviales, el framework (salvo smoke test de contexto), código muerto/legacy, tests que duplican la implementación.
5. **Pirámide**: ~60% unitarios (dominio primero: VOs/agregados) / ~25% integración (bordes de infraestructura) / ~15% E2E (flujos críticos).
6. **Tests especiales criptografía**: roundtrip obligatorio (`decrypt(encrypt(x)) == x`), IVs distintos por cifrado, HMAC determinista.

* Bien, porque la suite es navegable y auto-documentada; fallos localizados al escenario exacto; criterio anti-basura explícito.
* Malo, porque disciplina que mantener (skill `testing-standards` planificado ayuda); verbosidad de nombres (comprada a conciencia).

## Decisión

Opción elegida: **C**. Estado actual honesto: la capa de aplicación (use cases/mappers) tiene 0 tests — brecha priorizada P1 en [backlog](../../internal/backlog.md) con casos concretos identificados (`CreateOwnerUseCaseAdapterTest`, etc.).

### Justificación

Las convenciones convierten la suite en especificación ejecutable — coherente con el objetivo de que todo el repositorio enseñe. El criterio de "cuándo no testear" es tan importante como el de cuándo sí: evita la ilusión de cobertura.

## Consecuencias

* **Positivas**: suite navegable por diseño; fallos auto-explicativos; dominio con cobertura prioritaria.
* **Negativas**: nombres largos; deuda actual de capa application (planificada).
* **Neutras**: integración con Testcontainers pendiente para tests de infraestructura ([backlog](../../internal/backlog.md)).

## Validación

Review aplica las convenciones; skill `testing-standards` planificado ([ADR-0038](adr-0038-skills-opencode-guardianes.md)); métrica de cobertura revisada por fase del roadmap.

## Referencias

* [Guía interna de testing](../../internal/guides/testing-conventions.md)
* [ADR-0036](adr-0036-stack-testing-resultassertions-test-jar.md)
