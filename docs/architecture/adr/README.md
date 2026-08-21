# Architecture Decision Records (ADRs)

Los ADRs documentan cada decisión arquitectónica significativa de Jobby ERP: el contexto que la motivó, las opciones consideradas, la decisión tomada y sus consecuencias. Usamos el formato [MADR 3.0.0](https://adr.github.io/madr/) adaptado al español (ver [`template.md`](template.md)).

Este proyecto es, entre otras cosas, un recurso de aprendizaje: por eso los ADRs no solo registran *qué* se decidió, sino *por qué* y *qué alternativas fueron descartadas*. Leer la cadena de decisiones de un proyecto real es una de las formas más eficientes de entender arquitectura aplicada.

---

## Convenciones

### Numeración y archivos

- Numeración secuencial: `0001`, `0002`, ... — nunca se reutilizan números.
- Nombre de archivo: `adr-NNNN-titulo-corto-en-kebab-case.md`.
- Un archivo por decisión. Si una decisión evoluciona, se escribe un **nuevo** ADR que marca al anterior como `sustituida por`.

### Estados

| Estado | Significado |
|---|---|
| `propuesta` | Decisión intencional aún no implementada o en discusión |
| `aceptada` | Decisión vigente, aplicada en el código |
| `sustituida` | Reemplazada por otro ADR (se indica cuál). El histórico se conserva |

### Reglas de escritura

1. **Toda decisión significativa merece un ADR**: elección de framework, patrón estructural, convención transversal, tecnología de infraestructura, política de seguridad.
2. **No son ADRs**: decisiones triviales o reversibles sin costo (nombres locales, detalles de implementación encapsulados).
3. **Documentar descartes**: las opciones evaluadas y rechazadas se incluyen siempre; evitar repetir análisis ya hecho vale tanto como registrar la elección.
4. **Retro-documentación honesta**: los ADRs iniciales de este proyecto formalizan decisiones tomadas durante el desarrollo temprano; se marcan con fecha de formalización y referencias directas al código que las evidencia.
5. **Inmutabilidad**: salvo correcciones tipográficas, un ADR aceptado no se edita; se sustituye.
6. **Idioma**: español.

---

## Índice de ADRs

### Grupo A — Fundamentos arquitectónicos

| ADR | Decisión | Estado |
|---|---|---|
| [ADR-0001](adr-0001-registrar-decisiones-con-adrs.md) | Registrar decisiones de arquitectura con ADRs (MADR) | Aceptada |
| [ADR-0002](adr-0002-microservicios-con-database-per-service.md) | Microservicios con Database per Service | Aceptada |
| [ADR-0003](adr-0003-ddd-hexagonal-dominio-puro.md) | DDD + Arquitectura Hexagonal con dominio puro | Aceptada |
| [ADR-0004](adr-0004-monorepo-maven-multi-modulo.md) | Monorepo Maven multi-módulo mientras el equipo sea pequeño | Aceptada |
| [ADR-0005](adr-0005-shared-como-starter-autoconfiguracion.md) | Módulo `shared` como starter de autoconfiguración | Aceptada |
| [ADR-0006](adr-0006-cqrs-parcial.md) | CQRS parcial: commands/queries separados, mismo modelo | Aceptada |

### Grupo B — Stack tecnológico

| ADR | Decisión | Estado |
|---|---|---|
| [ADR-0007](adr-0007-java21-springboot-framework-default.md) | Java 21 + Spring Boot como framework por defecto | Aceptada |
| [ADR-0008](adr-0008-kafka-kraft-mensajeria.md) | Kafka 4.0 en modo KRaft como bus de mensajería | Aceptada |
| [ADR-0009](adr-0009-apicurio-wire-format-confluent.md) | Apicurio Registry con wire format compatible Confluent | Aceptada |
| [ADR-0010](adr-0010-avro-build-time-eventos.md) | Avro generado en build-time para eventos | Aceptada |
| [ADR-0011](adr-0011-mongodb-json-schema-indices-hmac.md) | MongoDB con JSON Schema strict e índices sobre HMAC | Aceptada |
| [ADR-0012](adr-0012-redis-cache-factory-propia.md) | Redis como caché con factory propia en `shared` | Aceptada |
| [ADR-0013](adr-0013-observabilidad-self-hosted.md) | Observabilidad self-hosted con Micrometer Observation | Aceptada |
| [ADR-0014](adr-0014-snowflake-ids-distribuidos.md) | Snowflake para IDs distribuidos | Aceptada |
| [ADR-0015](adr-0015-storage-s3-compatible-presigned.md) | Storage S3-compatible con presigned URLs | Aceptada |

### Grupo C — Patrones transversales

| ADR | Decisión | Estado |
|---|---|---|
| [ADR-0016](adr-0016-result-pattern-unico.md) | Result Pattern como mecanismo único de flujo de negocio | Aceptada |
| [ADR-0017](adr-0017-validationchain-perezosa.md) | ValidationChain perezosa con cortocircuito | Aceptada |
| [ADR-0018](adr-0018-taxonomia-errores-tres-niveles.md) | Taxonomía de errores de tres niveles con sanitización | Aceptada |
| [ADR-0019](adr-0019-fabrica-doble-of-reconstruct.md) | Fábrica doble `of()`/`reconstruct()` para VOs y agregados | Aceptada |
| [ADR-0020](adr-0020-buses-in-process-command-query-event.md) | Buses in-process Command/Query/Event sin reflexión | Aceptada |
| [ADR-0021](adr-0021-contratos-records-application.md) | Contratos como records inmutables en `application/` | Aceptada |
| [ADR-0022](adr-0022-eventos-dominio-pull-domain-events.md) | Eventos de dominio vía `pullDomainEvents()` | Aceptada |
| [ADR-0023](adr-0023-separacion-event-interno-domainevent-externo.md) | Separación Event interno vs DomainEvent externo | Aceptada |
| [ADR-0024](adr-0024-outbox-pattern-atomicidad-bd-kafka.md) | Outbox Pattern para atomicidad BD-Kafka | Propuesta |
| [ADR-0025](adr-0025-transacciones-explicitas-transaction-orchestrator.md) | Transacciones explícitas con TransactionOrchestrator | Aceptada |
| [ADR-0026](adr-0026-strategy-autodescubrimiento-spring.md) | Strategy con autodescubrimiento Spring | Aceptada |
| [ADR-0027](adr-0027-validacion-distribuida-por-capas.md) | Validación distribuida por capas sin duplicación | Aceptada |

### Grupo D — Seguridad

| ADR | Decisión | Estado |
|---|---|---|
| [ADR-0028](adr-0028-cifrado-campo-indice-hmac-searchable.md) | Cifrado a nivel de campo con índice HMAC searchable | Aceptada |
| [ADR-0029](adr-0029-pii-cifrada-end-to-end-kafka.md) | PII cifrada extremo a extremo en mensajes Kafka | Aceptada |
| [ADR-0030](adr-0030-sanitizacion-errores-support-id.md) | Sanitización de errores internos + supportId trazable | Aceptada |

### Grupo E — API y datos

| ADR | Decisión | Estado |
|---|---|---|
| [ADR-0031](adr-0031-problem-details-rfc7807.md) | Problem Details (RFC 7807) para API externa | Aceptada |
| [ADR-0032](adr-0032-nomenclatura-fqcn-topics-consumer-groups.md) | Nomenclatura FQCN invertido para topics y consumer groups | Aceptada |
| [ADR-0033](adr-0033-colecciones-separadas-users-owners-employees.md) | Colecciones separadas users/owners/employees | Aceptada |
| [ADR-0034](adr-0034-reference-data-cache-in-memory.md) | Reference data como caché in-memory read model | Aceptada |
| [ADR-0035](adr-0035-codigos-efimeros-redis.md) | Códigos efímeros en Redis con TTL y consumo único | Aceptada |

### Grupo F — Calidad y proceso

| ADR | Decisión | Estado |
|---|---|---|
| [ADR-0036](adr-0036-stack-testing-resultassertions-test-jar.md) | Stack de testing con ResultAssertions distribuido vía test-jar | Aceptada |
| [ADR-0037](adr-0037-convenciones-tests.md) | Convenciones de tests: estructura, nomenclatura y alcance | Aceptada |
| [ADR-0038](adr-0038-skills-opencode-guardianes.md) | Skills de OpenCode como guardianes automatizados | Propuesta |
| [ADR-0039](adr-0039-principio-explicitness-over-magic.md) | Principio "explicitness over magic" | Aceptada |

---

## Cómo proponer un nuevo ADR

1. Copia [`template.md`](template.md) con el siguiente número disponible.
2. Completa todas las secciones; incluye al menos dos opciones consideradas.
3. Marca estado `propuesta` y abre Pull Request.
4. La discusión ocurre en el PR; al aprobarse, el estado pasa a `aceptada`.
