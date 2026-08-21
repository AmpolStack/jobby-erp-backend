# ADR-0013: Observabilidad self-hosted con Micrometer Observation

* Fecha: 2026-08-20 (retro-documentada)
* Estado: aceptada

## Contexto y problema

Un sistema distribuido sin observabilidad es un sistema que falla a ciegas. La promesa del producto ("diseñado para no caerse nunca") exige detectar degradaciones antes que los usuarios, correlacionar errores reportados con trazas concretas y medir tanto infraestructura como negocio.

Había que elegir: la API de instrumentación en código y el stack de backend (métricas, logs, trazas).

## Impulsores de la decisión

* Una sola envoltura de código debe producir métricas Y trazas simultáneamente.
* Correlación logs ↔ trazas ↔ métricas por `traceId`.
* Stack open source self-hosted, coherente con la filosofía del proyecto.
* Métricas de negocio (usuarios registrados, emails enviados) tan first-class como las técnicas.

## Opciones consideradas

### Opción A: Instrumentación manual separada (Micrometer core + OpenTelemetry API por separado)

* Bien, porque control fino de cada señal.
* Malo, porque duplica esfuerzo: cada operación requiere escribir métrica Y span por separado; divergencia garantizada con el tiempo.

### Opción B: APM comercial (Datadog, New Relic)

* Bien, porque observabilidad llave-en-mano.
* Malo, porque costo recurrente por host/span inviable para un proyecto open source; vendor lock-in; contradice la filosofía self-hosted.

### Opción C: Micrometer Observation + stack self-hosted

API `Observation` de Micrometer como única envoltura: cada observation produce timer/counter Prometheus **y** child span Zipkin simultáneamente. Backend: Prometheus (métricas) + Loki (logs) + Zipkin (trazas) + Grafana (visualización), todo en Docker Compose con dashboard pre-provisionado.

* Bien, porque una envoltura → dos señales; correlación nativa vía MDC (`%X{traceId},%X{spanId}` en logback); stack 100% open source reproducible localmente.
* Malo, porque operación propia del stack (aceptada: compose listo con datasources provisionados).

## Decisión

Opción elegida: **C**. Reglas operativas:

1. **Dónde**: todos los adapters de infraestructura inyectan `ObservationRegistry` — Redis, Kafka publisher, Email/SMTP, S3 FileStorage, MongoDB.
2. **Nomenclatura por operación**: `redis.register/get/remove`, `kafka.publish`, `kafka.publish-async`, `email.send`, `filestorage.upload/get-signed/delete`, `mongodb.read/write`. `observation.stop()` = éxito; `observation.error(e)` = fallo con tag `error`.
3. **Cardinalidad**: tags indexables de baja cardinalidad (`bucket`, `topic`) vía `lowCardinalityKeyValue`; alta cardinalidad (`key` de S3, `traceId`) solo visible en Zipkin.
4. **Logs ≠ observations**: no se mezclan; los logs de adapters llevan prefijo `[ERROR_TYPE]` alineado con la taxonomía ([ADR-0018](adr-0018-taxonomia-errores-tres-niveles.md)).
5. **Negocio**: métricas con `MeterRegistry` directo en casos de uso (`users.registered.total{method}`, `users.login.failure{reason}`, Timer `usecase.execution.duration{usecase}`).
6. **Pipeline del bus**: decorador `MetricsCommandBusDecorator` con Timer `command.bus.duration` ([ADR-0020](adr-0020-buses-in-process-command-query-event.md)).

Logback centralizado en `shared` con doble appender (consola + Loki via loki4j) y labels `app=${APP_NAME},level=%level`.

### Justificación

Micrometer Observation es la respuesta canónica de la JVM al problema "dos señales, un punto de instrumentación". El stack self-hosted completo en Compose hace que cualquier contribuyente tenga la misma visibilidad que producción desde el día uno.

## Consecuencias

* **Positivas**: correlación total traceId↔log↔métrica; costos cero de licenciamiento; dashboard único en Grafana.
* **Negativas**: disciplina de nomenclatura requerida (catálogo definido en [guía de observabilidad](../../internal/guides/observability-guide.md)); algunos adapters pendientes de instrumentar (deuda registrada).
* **Neutras**: HealthIndicators custom para SMTP/S3 propuestos pero aún no creados ([backlog](../../internal/backlog.md)).

## Validación

Dashboard "spring-boot-observability" pre-provisionado en Grafana; verificación manual de spans en Zipkin; la guía interna define el checklist de instrumentación por adapter.

## Referencias

* [Guía interna de observabilidad](../../internal/guides/observability-guide.md)
* [ADR-0030](adr-0030-sanitizacion-errores-support-id.md) — supportId basado en traceId
