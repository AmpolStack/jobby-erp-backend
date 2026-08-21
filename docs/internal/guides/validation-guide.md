# Guía de Validación y Manejo de Errores

* Tipo: guía interna (how-to) · Audiencia: contribuyentes · Última actualización: 2026-08-20
* Decisiones subyacentes: [ADR-0016](../../architecture/adr/adr-0016-result-pattern-unico.md), [ADR-0017](../../architecture/adr/adr-0017-validationchain-perezosa.md), [ADR-0018](../../architecture/adr/adr-0018-taxonomia-errores-tres-niveles.md), [ADR-0027](../../architecture/adr/adr-0027-validacion-distribuida-por-capas.md)

---

## Result Pattern — reglas de uso

Todo flujo de negocio retorna `Result<T, Error>`. Prohibido lanzar excepciones por reglas de negocio. La única excepción lanzable es `InconsistencyResultException` (bug de programación, nunca validación de entrada).

API disponible en `Result<T,E>` (sealed interface con records `Success`/`Failure`):

| Operación | Uso |
|---|---|
| `success(v)` / `failure(type, field)` | Fábricas |
| `map(f)` | Transforma el éxito |
| `flatMap(f)` | Encadena operaciones que retornan Result |
| `peek(consumer)` | Efecto lateral sobre el éxito sin transformar |
| `fold(onSuccess, onFailure)` | Colapsa a un valor único |
| `propagateFailure(newField)` | Re-etiqueta el field al propagar (ver abajo) |

**Regla de propagación:** los códigos `ITN_*` conservan el nombre de campo original; el resto lo reemplaza — evita filtrar nombres de campos internos.

### Semántica null/vacío

Decidida por operación ([ADR-0017](../../architecture/adr/adr-0017-validationchain-perezosa.md)):

- Si la operación tiene sentido con entrada vacía → **success** con valor vacío (`encryptAsBytes(null)` → `byte[0]`).
- Si no (`hash(null)`) → **failure** con `validateInternalNotBlank`.

---

## ValidationChain

Validación perezosa con cortocircuito: la cola de suppliers no ejecuta hasta `build()`; la primera falla detiene todo.

```java
ValidationChain.create()
        .validateNotBlank(value, fieldName)
        .build()
        .flatMap(v -> ValidationChain.create()
                .validateGreaterOrEqualsThan(clean.length(), MIN, fieldName)
                .validateSmallerOrEqualsThan(clean.length(), MAX, fieldName)
                .build()
                .map(v2 -> new Name(clean)));
```

Convenciones:

1. Validaciones generales (null/blank) primero; específicas después.
2. `validateIf(condición, supplier)` para reglas condicionales.
3. Dos familias de validadores — elegir según audiencia:

| Familia | Produce | Audiencia |
|---|---|---|
| `validateNotNull`, `validateEmail`, ... | Códigos visibles (`VALIDATION_ERROR`) | Usuario final |
| `validateInternalNotNull`, ... | Códigos `ITN_*` | Interna — se sanitizan antes de exponerse |

---

## Taxonomía de errores

Ver decisión completa en [ADR-0018](../../architecture/adr/adr-0018-taxonomia-errores-tres-niveles.md).

| Prefijo | Significado | Tratamiento |
|---|---|---|
| *(ninguno)* | Error legítimo del usuario | Mensaje mostrable tal cual |
| `ITN_*` | Validación interna (solo rompible por bug) | Sanitizado antes de exponer |
| `ITS_*` | Fallo de sistema/configuración | Sanitizado + log nivel error |

- `toSanitizedError()` reemplaza detalle real por mensaje genérico y loguea.
- Mapeo ErrorType→HttpStatus vía `EnumMap`; tipos expuestos registrados en `ErrorTypeHttpCollection`.
- Logs de adapters llevan prefijo `[ERROR_TYPE]` correlacionado.

---

## Validación distribuida por capas

Cada chequeo vive en **exactamente una capa** ([ADR-0027](../../architecture/adr/adr-0027-validacion-distribuida-por-capas.md)). "Si ves el mismo chequeo en dos flechas, uno sobra."

```
Config ──────────── valida propiedades propias (fail-fast startup, Jakarta Validation)
Controller ──────── valida estructura HTTP del request (SafeResultValidator)
CommandMapper ───── convierte crudos → VOs vía of()  ← dispara la validación de VOs
Value Object ────── valida reglas intrínsecas del dato
Aggregate ───────── valida consistencia interna
Use Case ────────── valida reglas con repositorios (unicidad, existencia)
Servicio/Adapter ── valida solo sus entradas (nunca su configuración)
```

### Errores comunes a evitar

1. **Validar y descartar**: construir el VO validado con `of()` pero pasar el string crudo a la factory → siempre propagar el VO.
2. **Unicidad fuera del use case**: `existByEmail` vive solo en el caso de uso.
3. **Re-validar en reconstruct()**: datos de BD son confiables por definición.
4. **Validar configuración en runtime del servicio**: ya fue validada fail-fast en startup (`CryptoUtils.validateAndParseKey`, `validateConfig()`).
5. **HttpMappers que validan**: solo reorganizan campos.

---

## Jakarta Validation — única excepción permitida

La validación declarativa por anotaciones con excepción lanzada está permitida **solo** para:

- `@ConfigurationProperties` (fail-fast en startup, antes de servir tráfico).
- Opcionalmente requests REST como filtro estructural temprano.

Fuera de esos puntos, todo pasa por Result + ValidationChain.

---

## Checklist rápido

- [ ] ¿La regla nueva tiene exactamente una capa dueña?
- [ ] ¿El validador elegido es `validate*` (usuario) o `validateInternal*` (interno)?
- [ ] ¿El mapper propaga VOs, no raw strings?
- [ ] ¿El error nuevo está registrado en `ErrorType` y mapeado a HTTP?
