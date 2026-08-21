# Guía de Observabilidad

* Tipo: guía interna (how-to) · Audiencia: contribuyentes · Última actualización: 2026-08-20
* Decisión subyacente: [ADR-0013](../../architecture/adr/adr-0013-observabilidad-self-hosted.md)

---

## Stack

| Señal | Tecnología | Puerto |
|---|---|---|
| Métricas | Prometheus | — |
| Logs | Loki (via loki4j) | — |
| Trazas | Zipkin | — |
| Visualización | Grafana (datasources + dashboard provisionados) | — |

Dashboard pre-provisionado: `shared/docker/sources/grafana/dashboards/spring-boot-observability.json`.

**API única:** Micrometer `Observation` — una envoltura produce timer/counter Prometheus **y** child span Zipkin simultáneamente. Dependencia `micrometer-observation` explícita en `shared/pom.xml`.

---

## Cómo instrumentar un adapter

1. Inyectar `ObservationRegistry` por constructor (constructores manuales cuando el orden de campos no es obvio — no `@AllArgsConstructor`).
2. Envolver la operación:

```java
Observation.createNotStarted("mongodb.write", observationRegistry)
    .lowCardinalityKeyValue("collection", "users")
    .observe(() -> /* operación */);
```

3. `observation.stop()` = éxito; `observation.error(e)` = fallo con tag `error`.

### Catálogo de observaciones

| Adapter | Observación |
|---|---|
| Redis | `redis.register`, `redis.get`, `redis.remove` |
| Kafka publisher | `kafka.publish`, `kafka.publish-async` |
| Email/SMTP | `email.send` |
| FileStorage S3 | `filestorage.upload`, `filestorage.get-signed`, `filestorage.delete` |
| MongoDB | `mongodb.read`, `mongodb.write` |
| Buses (decorador) | Timer `command.bus.duration` con tags `command`/`success` |

---

## Cardinalidad de tags

| Tipo | API | Ejemplos | Destino |
|---|---|---|---|
| Baja (indexable) | `lowCardinalityKeyValue` | `bucket`, `topic`, `collection` | Prometheus |
| Alta | atributos normales | `key` de S3, `traceId` | Solo Zipkin |

Regla: si el valor tiene infinitas posibilidades, nunca como tag de métrica.

---

## Logs

- Logback centralizado en `shared` (`logback-spring.xml`): doble appender consola + Loki.
- Patrón con correlación: `%X{traceId},%X{spanId}`.
- Labels Loki: `app=${APP_NAME},level=%level`.
- Propiedad `app.logging.loki-url` documentada en `additional-spring-configuration-metadata.json` (autocomplete IDE).

### Convención de prefijos de error

Los logs de adapters llevan el `[ERROR_TYPE]` alineado con la taxonomía ([ADR-0018](../../architecture/adr/adr-0018-taxonomia-errores-tres-niveles.md)):

```
[ITS_SERIALIZATION_ERROR] ...
[ITS_EXTERNAL_SERVICE_FAILURE] ...
[VALIDATION_ERROR] ...
```

Logs y Observation **no se mezclan**: son canales complementarios.

---

## Métricas de negocio

Con `MeterRegistry` directo en Application Services — miden eventos de dominio, no infraestructura:

| Métrica | Tags |
|---|---|
| `users.registered.total` | `method` |
| `users.login.failure` | `reason` |
| `emails.welcome.sent` | — |
| `usecase.execution.duration` (Timer) | `usecase` |
| `cache.hit` / `cache.miss` | — (medidas en wrapper decorator, no en el adapter: el adapter mide si Redis respondió; el wrapper mide si el dato existía) |

Exportación automática vía `micrometer-registry-prometheus`.

---

## Health indicators

- Los nativos de Spring cubren los componentes estándar.
- Pendientes de crear: `SmtpHealthIndicator` y `S3HealthIndicator` en `shared/.../health/`, registrados con `@ConditionalOnMissingBean` ([backlog](../backlog.md)).

---

## Caso de estudio: MongoDB

Se evaluó un wrapper decorator para instrumentar Mongo y se **descartó** por simplicidad: se eligió agregar constructor + Observation directamente a `MongoDbSpringDataHandler`. Ese handler además traduce sistemáticamente excepciones Spring DAO → ErrorType (~10 catch por operación: `DuplicateKeyException`→VALIDATION_ERROR, `OptimisticLockingFailureException`→fallo transitorio, etc.), cada uno con su observación y log `[ITS_*]`.

---

## Checklist para un adapter nuevo

- [ ] ¿`ObservationRegistry` inyectado por constructor?
- [ ] ¿Nombre de observación registrado en el catálogo de arriba?
- [ ] ¿Tags de baja cardinalidad solo donde corresponde?
- [ ] ¿Logs con prefijo `[ERROR_TYPE]`?
- [ ] ¿HealthIndicator necesario? → crearlo condicional
