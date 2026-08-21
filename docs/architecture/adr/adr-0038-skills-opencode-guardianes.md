# ADR-0038: Skills de OpenCode como guardianes automatizados de convenciones

* Fecha: 2026-08-20
* Estado: propuesta

## Contexto y problema

Las convenciones del proyecto (pureza del dominio, fábrica doble, Result obligatorio, ordeñe de eventos, validación por capa) se garantizan hoy por code review humano. Eso escala mal: el review se concentra en lógica y deja pasar violaciones mecánicas; los contribuyentes nuevos cometen errores evitables; y las convenciones viven en documentos que nadie releerá en el momento justo.

El proyecto usa OpenCode como asistente de desarrollo. Sus skills (instrucciones activables `.opencode/skills/<name>/SKILL.md`) pueden convertir convenciones documentadas en verificaciones automáticas en el flujo de trabajo.

## Impulsores de la decisión

* Detectar violaciones mecánicas antes del review humano.
* Reducir la carga cognitiva del reviewer hacia lógica de negocio.
* Acelerar onboarding: el asistente corrige mientras escribes.
* Las convenciones ya están documentadas — falta operacionalizarlas.

## Opciones consideradas

### Opción A: Solo code review humano

* Bien, porque cero configuración.
* Malo, porque violaciones mecánicas compiten con lógica por la atención del reviewer; inconsistencia entre reviewers; escalado lineal con el equipo.

### Opción B: Reglas estáticas (ArchUnit) en CI

* Bien, porque deterministas y bloqueantes.
* Malo, porque cubre solo reglas estructurales (imports, dependencias) — no convenciones semánticas (¿este caso de uso ordeña eventos? ¿este VO usa la fábrica doble correctamente?).

### Opción C: Skills de OpenCode como guardianes

11 skills definidos en el catálogo ([guía interna](../../internal/guides/opencode-skills.md)):

| Skill | Verifica |
|---|---|
| `ddd-hexagonal-check` | Pureza de capas, puertos como interfaces, dependencias hacia adentro |
| `aggregate-invariants` | Invariantes de agregados, modificaciones vía raíz, ValidationChain |
| `value-object-immutabilidad` | VOs inmutables, autovalidados, equals/hashCode completos |
| `use-case-pattern` | Puerto + adapter + Result + orquestación estándar |
| `result-pattern-usage` | Uso correcto de Result/ValidationChain, prohibición de throw de negocio |
| `mapstruct-usage` | Ubicación y uso correcto de mappers |
| `testing-standards` | Convenciones de [ADR-0037](adr-0037-convenciones-tests.md) |
| `security-sensitive-data` | Campos sensibles con políticas correctas, PII cifrada |
| `problem-details-errors` | Respuestas HTTP según [ADR-0031](adr-0031-problem-details-rfc7807.md) |
| `transaction-consistency` | Eventos fuera de transacciones, orquestador usado correctamente |
| `domain-events` | Todo caso de uso que muta agregados llama `pullDomainEvents()` |

* Bien, porque verifica lo estructural Y lo semántico en el momento de escribir; complementa (no reemplaza) ArchUnit futuro; cada skill referencia su documento fuente.
* Malo, porque depende del asistente activado (no bloquea CI por sí solo); requiere mantener los skills sincronizados con las guías.

## Decisión

Opción elegida: **C**, con arquitectura complementaria:

1. **Skills OpenCode** = primera línea, durante la escritura del código.
2. **ArchUnit** (futuro) = segunda línea, bloqueante en CI para reglas estructurales.
3. **Code review** = tercera línea, enfocada en lógica y diseño.

Estado: los 11 skills están especificados pero **ninguno creado aún** como archivo `SKILL.md` — esta decisión formaliza el compromiso y el catálogo. La implementación es tarea priorizada del [backlog](../../internal/backlog.md).

### Justificación

Defensa en profundidad adaptada al flujo real de desarrollo: cuanto antes se detecte la violación, más barato el fix. Los skills son la única opción que opera *antes* del commit sin infraestructura de CI adicional.

## Consecuencias

* **Positivas**: convenciones aplicadas en tiempo de escritura; review enfocado; onboarding asistido.
* **Negativas**: mantenimiento doble (guía ↔ skill) mitigado por referencia cruzada; cobertura parcial si un dev no usa OpenCode (por eso las 3 líneas).
* **Neutras**: ArchUnit entrará cuando el número de módulos justifique CI más estricto.

## Validación

Métrica informal: violaciones mecánicas detectadas en review deberían tender a cero tras activar los skills; revisión trimestral del catálogo contra las guías.

## Referencias

* [Catálogo completo de skills](../../internal/guides/opencode-skills.md)
