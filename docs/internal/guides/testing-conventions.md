# Guía de Testing — Convenciones

* Tipo: guía interna (how-to) · Audiencia: contribuyentes · Última actualización: 2026-08-20
* Decisiones subyacentes: [ADR-0036](../../architecture/adr/adr-0036-stack-testing-resultassertions-test-jar.md), [ADR-0037](../../architecture/adr/adr-0037-convenciones-tests.md)

---

## Stack

| Herramienta | Uso |
|---|---|
| JUnit Jupiter 5.11 | Framework base |
| AssertJ 3.27 | Aserciones generales (solo lo que ResultAssertions no cubre) |
| Mockito 5 | Mocks de puertos de salida en tests de casos de uso |
| `ResultAssertions` | Toda aserción sobre `Result<T, Error>` |
| Paquete `boundaries` | Datos de frontera reutilizables para `@ParameterizedTest` |

Distribución: `shared` publica test-jar (`maven-jar-plugin` goal `test-jar`); los servicios dependen con scope `test`.

### ResultAssertions

```java
assertSuccess(result);
assertSuccess(result, expectedValue);
assertFailure(result);
assertFailure(result, ErrorType.VALIDATION_ERROR);   // código exacto con assertSame
assertFailure(result, expectedError);
```

### Boundaries

Proveedores como `Stream<Arguments>`: `EmailBoundaries`, `PhoneBoundaries`, `NullityBoundaries`, etc.

Regla de uso restrictivo: usar boundaries solo si el set se repite en múltiples tests; para 1-2 casos, inline con `@ValueSource`.

---

## Estructura y nomenclatura

```java
@DisplayName("Name Value Object")
class NameTest {

    @Nested
    @DisplayName("Given valid input → When of() → Then returns success")
    class OfMethod {
        @Test
        void of_WhenValidName_ShouldReturnSuccess() { ... }

        @ParameterizedTest
        @MethodSource("com.jobby.boundaries.EmailBoundaries#invalid")
        void of_WhenInvalidEmail_ShouldReturnValidationError(String raw) { ... }
    }
}
```

1. **Un `@Nested` por método bajo prueba** (`OfMethod`, `CreateMethod`).
2. **Nombre**: `methodName_WhenCondition_ShouldExpectedBehavior` — Given-When-Then implícito.
3. **`@DisplayName`**: frase legible "Given [condición] → Then [resultado]".
4. **Prohibidos**: `testEncrypt()`, `shouldWork()`, nombres sin escenario.

## Selección de tipo de test

| Situación | Herramienta |
|---|---|
| Variaciones del mismo escenario | `@MethodSource` + boundaries |
| Rangos de valores válidos | `@ValueSource` |
| Aleatoriedad / determinismo | `@RepeatedTest` |
| Un caso puntual | `@Test` |

**Un escenario por test**: los fallos deben localizarse al escenario exacto.

---

## Qué NO testear

- Getters/setters triviales.
- El framework mismo (salvo smoke test de contexto — existe `contextLoads`).
- Código muerto o legacy.
- Tests que duplican la implementación (asertar el "cómo" en vez del "qué").

---

## Tests especiales por dominio

### Criptografía ([guía de seguridad](security-guide.md))

- Roundtrip obligatorio: `decryptFromBytes_AfterEncryptAsBytes_ShouldReturnOriginalPlaintext`.
- IVs distintos por cifrado (probabilístico).
- HMAC determinista (misma entrada → mismo índice).

### Eventos de dominio ([guía de eventos](domain-events-guide.md))

Los 4 tests obligatorios por caso de uso con eventos:

1. `create()` emite el evento esperado.
2. `reconstruct()` NO emite eventos.
3. Publicación solo tras transacción exitosa.
4. Sin publicación ante fallo de transacción.

---

## Pirámide objetivo

```
      ╱ E2E 15% ╲        flujos críticos punta a punta
    ╱ Integración ╲      bordes de infraestructura (25%)
  ╱   Unitarios 60% ╲    dominio primero: VOs, agregados, Result
```

Estado actual honesto: la capa application (use cases/mappers) tiene 0 tests — prioridad P1 en el [backlog](../backlog.md).

---

## Checklist de PR

- [ ] ¿Todo `Result` asertado vía `ResultAssertions`?
- [ ] ¿Nomenclatura GWT respetada?
- [ ] ¿Un escenario por test?
- [ ] ¿Roundtrip criptográfico donde aplique?
- [ ] ¿Tests de eventos completos donde aplique?
