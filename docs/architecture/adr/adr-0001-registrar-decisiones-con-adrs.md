# ADR-0001: Registrar decisiones de arquitectura con ADRs (MADR)

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

Jobby ERP tiene un objetivo explícito de servir como recurso de aprendizaje de microservicios aplicados a un producto real. Durante el desarrollo temprano se tomaron decenas de decisiones significativas — elección de mensajería, patrones de manejo de errores, estrategias de eventos de dominio — registradas en documentos sueltos, análisis ad-hoc y comentarios de código, sin un formato común ni trazabilidad.

Sin un registro estandarizado:

- Las alternativas evaluadas y descartadas se pierden, forzando a re-analizar lo mismo en el futuro.
- Un contribuyente nuevo no puede entender *por qué* el sistema es como es.
- Las decisiones tomadas en conversaciones o análisis temporales quedan sin dueño ni fecha.

## Impulsores de la decisión

* El proyecto se construye en público y debe ser estudiable por terceros.
* Las decisiones arquitectónicas tienen consecuencias de largo plazo que deben ser auditables.
* La documentación existente estaba fragmentada en ~19 archivos sin estructura común.

## Opciones consideradas

### Opción A: Wiki libre / documentos narrativos

Documentos extensos por tema sin formato fijo.

* Bien, porque permite profundidad ilimitada.
* Malo, porque no distingue decisión de explicación; difícil de mantener y de saber qué está vigente.

### Opción B: Solo comentarios de código / ADRs ligeros de una línea

* Bien, porque cuesta casi nada mantenerlo.
* Malo, porque pierde contexto, alternativas y consecuencias — justo lo que da valor educativo.

### Opción C: MADR 3.0.0 (Markdown Any Decision Records)

Formato estructurado en Markdown: contexto, impulsores, opciones con pros/contras, decisión, consecuencias, validación.

* Bien, porque vive junto al código (versionable, revisable en PRs), fuerza a documentar alternativas, y es el estándar de facto de la comunidad ADR.
* Malo, porque exige disciplina para cada decisión (costo asumido deliberadamente).

## Decisión

Opción elegida: **MADR 3.0.0 adaptado al español**, con plantilla propia ([`template.md`](template.md)), numeración secuencial, estados `propuesta`/`aceptada`/`sustituida` y convenciones definidas en [`README.md`](README.md).

### Justificación

MADR equilibra rigor y bajo costo: es suficientemente estructurado para auditar decisiones y suficientemente simple para mantenerse en Markdown dentro del repo. El objetivo educativo del proyecto hace que documentar descartes sea tan valioso como registrar elecciones — MADR lo obliga por diseño.

## Consecuencias

* **Positivas**: trazabilidad completa de decisiones; onboarding acelerado; material base para guías públicas y artículos; historial de alternativas evita re-análisis.
* **Negativas**: costo disciplinario por decisión (~30-60 min por ADR).
* **Neutras**: los ADRs retroactivos marcan fecha de formalización, no de decisión original.

## Validación

Regla operativa: toda PR que introduzca una decisión nueva sin su ADR correspondiente se devuelve en review. Los skills automatizados de OpenCode ([ADR-0038](adr-0038-skills-opencode-guardianes.md)) verificarán esta regla.

## Referencias

* [MADR — Markdown Any Decision Records](https://adr.github.io/madr/)
* [Principios del proyecto](../../project/principles.md)
