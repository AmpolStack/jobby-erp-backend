# ADR-0017: ValidationChain perezosa con cortocircuito

* Fecha: 2026-08-20 (retro-documentada)
* Estado: aceptada

## Contexto y problema

Validar entradas es la operación más repetida del sistema: cada VO, cada agregado, cada caso de uso valida múltiples reglas. El enfoque ingenuo — una cascada de `if (invalid) return failure(...)` — produce métodos de 40 líneas donde la lógica real es invisible, y el enfoque acumulativo (validar todo y devolver lista de errores) complica el contrato y rara vez aporta al usuario.

Además existía un requisito sutil: algunas validaciones son *internas* (invariantes que solo pueden romperse por bug) y sus mensajes nunca deben llegar crudos al usuario.

## Impulsores de la decisión

* Legibilidad: las validaciones declaradas como datos, no como control de flujo.
* Cortocircuito: fallar temprano sin ejecutar validaciones posteriores.
* Doble audiencia del error: usuario final vs desarrollador.
* Composición con Result ([ADR-0016](adr-0016-result-pattern-unico.md)).

## Opciones consideradas

### Opción A: Cascadas de if/return

* Bien, porque cero abstracción.
* Malo, porque ruido estructural masivo; imposible de reutilizar; mezcla reglas de usuario e internas sin distinción.

### Opción B: Bean Validation (anotaciones Jakarta) como mecanismo general

* Bien, porque declarativo y familiar.
* Malo, porque expresividad limitada para reglas compuestas; errores como excepción (`MethodArgumentNotValidException`) chocan con Zero Exceptions ([ADR-0016](adr-0016-result-pattern-unico.md)); no distingue audiencias.

### Opción C: ValidationChain perezosa con doble familia de validadores

Cola de `Supplier<Result>` que no ejecuta nada hasta `build()`; la primera falla cortocircuita. Dos familias: `validate*` (errores visibles al usuario) y `validateInternal*` (errores `ITN_*` sanitizados antes de exponerse). Incluye `validateIf(condición, supplier)` para reglas condicionales.

* Bien, porque declara intención en cadena fluida; ejecución diferida permite construir sin efectos; la familia interna codifica la política de exposición en el propio nombre del método.
* Malo, porque abstracción propia que mantener (~270 líneas); requiere aprender la convención de orden.

## Decisión

Opción elegida: **C**. Convención de orden: validaciones generales (null/blank) primero, específicas después. Excepción única al ecosistema Result: Jakarta Validation declarativa para `@ConfigurationProperties` (fail-fast en startup), donde lanzar es correcto porque ocurre antes de servir tráfico.

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

### Justificación

La cadena convierte la parte más mecánica del código en su parte más legible, y la distinción `validate*`/`validateInternal*` resuelve a nivel de API el problema de seguridad de información ([ADR-0030](adr-0030-sanitizacion-errores-support-id.md)): no se puede filtrar accidentalmente lo que se marca como interno en origen.

## Consecuencias

* **Positivas**: validaciones legibles y reutilizables; cortocircuito garantizado; política de exposición integrada; tests parametrizados naturales ([ADR-0037](adr-0037-convenciones-tests.md)).
* **Negativas**: anidamiento flatMap cuando hay dependencia entre pasos; componente core que exige tests exhaustivos (los tiene).
* **Neutras**: semántica null/vacío decidida por operación: si vacío tiene sentido → success con valor vacío; si no → failure con validador interno.

## Validación

Suite de tests dedicada sobre ValidationChain; uso verificado en review para toda nueva validación; skill `result-pattern-usage` planificado cubre su uso correcto.

## Referencias

* [Guía interna de validación](../../internal/guides/validation-guide.md)
* [ADR-0027](adr-0027-validacion-distribuida-por-capas.md) — dónde vive cada validación
