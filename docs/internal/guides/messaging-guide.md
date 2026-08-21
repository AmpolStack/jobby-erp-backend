# Guía de Mensajería — Kafka, Apicurio y Avro

* Tipo: guía interna (how-to) · Audiencia: contribuyentes · Última actualización: 2026-08-20
* Decisiones subyacentes: [ADR-0008](../../architecture/adr/adr-0008-kafka-kraft-mensajeria.md), [ADR-0009](../../architecture/adr/adr-0009-apicurio-wire-format-confluent.md), [ADR-0010](../../architecture/adr/adr-0010-avro-build-time-eventos.md), [ADR-0029](../../architecture/adr/adr-0029-pii-cifrada-end-to-end-kafka.md), [ADR-0032](../../architecture/adr/adr-0032-nomenclatura-fqcn-topics-consumer-groups.md)

> Unifica el contenido de los análisis históricos `kafka-changes-summary` y
> `apicurio-confluent-wire-format` más hallazgos de código.

---

## Infraestructura local

| Componente | Detalle |
|---|---|
| Kafka 4.0 KRaft | 1 controlador + 3 brokers, sin ZooKeeper; healthchecks de quórum |
| Apicurio Registry 3.x | Storage `kafkasql` (topics internos `kafkasql-journal`/`kafkasql-snapshots`, RF=3); puerto 8091 |
| kafbat (Kafka UI) | Inspección visual; puerto 8092 |
| Redes Docker | `jobby-messaging-network` nombrada — cualquier servicio futuro se conecta |

Compose raíz: `shared/docker/docker-compose.yml` (incluye mensajería + monitoreo).

---

## Wire format compatible Confluent

El formato en el cable es `[0x00][4B contentId][Avro]` — idéntico al estándar de facto Confluent — para que kafbat/AKHQ deserialicen sin código custom.

Propiedades (producer y consumer):

```yaml
apicurio.registry.use-id: contentId
apicurio.registry.id-handler: io.apicurio.registry.serde.Legacy4ByteIdHandler
```

### Configuración asimétrica producer/consumer

```yaml
# Producer — serializador propio dual
apicurio.registry.headers.enabled: true
apicurio.registry.serde.avro.auto-register: true

# Consumer — deserializador estándar
apicurio.registry.headers.enabled: false   # lee el ID del payload, ignora headers
```

### DualAvroKafkaSerializer

Ubicación: `shared/.../serde/DualAvroKafkaSerializer.java`. Extiende `AvroKafkaSerializer` de Apicurio y sobrescribe `serialize()`:

1. Escribe magic byte + contentId de 4 bytes en el payload → **compatibilidad kafbat**.
2. Llama `serializeData()` para el payload Avro.
3. Llama `headersHandler.writeHeaders()` → **metadatos visibles en UI**.

El consumer con `headers.enabled: false` ignora headers y funciona sin cambios.

---

## Metadatos en headers de Kafka

Construidos por `EventPublisherAdapter` desde el `DomainEvent`:

| Header | Valor | Ejemplo |
|---|---|---|
| `eventId` | UUID del evento | `550e8400-e29b-41d4-a716-446655440000` |
| `occurredOn` | Timestamp ISO | `2026-06-23T12:00:00Z` |
| `aggregateType` | Tipo de agregado | `user` |
| `operationType` | Tipo de operación | `CREATE` |

`aggregateId` se excluye porque viaja como key del mensaje.

---

## Avro — esquemas y generación

- Esquemas `.avsc` versionados en `src/main/resources/avro/<nombre>-event.avsc`.
- `avro-maven-plugin 1.12.0` genera POJOs en fase `generate-sources` con `stringType=String`.
- Package destino: `com.jobby.<servicio>.events`.

**Regla PII:** todo campo sensible es tipo `bytes` y viaja cifrado ([ADR-0029](../../architecture/adr/adr-0029-pii-cifrada-end-to-end-kafka.md)). Ejemplo vivo: `created-user-event.avsc` declara `name`/`email` como bytes; la Transformation cifra antes de serializar.

---

## Transformaciones dominio ↔ Avro

Cada evento tiene su `Transformation<DominioEvent, AvroSchema>` bidireccional registrada en `TransformationRegistry`. Las transformaciones:

- Son clases abstractas que se auto-registran leyendo sus genéricos por reflexión en el constructor — las subclases solo declaran `extends Transformation<A,B>`.
- Viven en `infrastructure/transformations/`.
- Cifran/descifran campos PII usando los puertos criptográficos.

No usar MapStruct para estas conversiones: MapStruct es field mapping puro; las transformaciones orquestan seguridad + validación + Result.

---

## Routing y publicación

- `EventRegistry` + `EventRoute<D>(route, destinyClass)`: mapea clase de evento → topic + clase Avro destino.
- `MessagingPublisher` (puerto) con métodos sync (`future.get` con timeout) y async (`whenComplete`); versión extendida acepta metadata para headers.
- Implementación `KafkaMessagingPublisher`: observaciones `kafka.publish` / `kafka.publish-async` con tag low-cardinality `topic`; mapeo de excepciones a `ITS_EXTERNAL_SERVICE_FAILURE` / `ITS_OPERATION_ERROR`.
- Timeout actual de publish: 5 segundos hardcodeado (deuda conocida — ver [backlog](../backlog.md)).

---

## Nomenclatura

```
Topics:          com.jobby.<servicio-productor>.<acción>
Consumer groups: com.jobby.<servicio-consumidor>.<acción>-consumer
Ejemplos:        com.jobby.user.create-user
                 com.jobby.user-service.create-user-consumer
```

---

## Configuración de topics y reintentos

- Producer idempotente, `acks=all`; consumer commit manual, `auto-offset-reset: earliest`.
- Reintentos: `@RetryableTopic` — 4 intentos, backoff exponencial 2s × 2.0.
- DLT: sufijo `.DLT` para inspección manual de mensajes que agotan reintentos.

---

## Plan de rollback ante cambios de mensajería

1. Revertir el serializer custom al estándar Apicurio.
2. Revertir propiedades YAML (`use-id`, `id-handler`, `headers.enabled`).
3. Eliminar métodos de metadata del publisher.
4. Revertir renombres (`EventPublisherAdapter` ↔ `KafkaEventPublisher`).
5. Verificar roundtrip con tests Avro.
6. Validar inspección en kafbat.

---

## Checklist para un evento nuevo

- [ ] `.avsc` creado (PII como `bytes`) + POJO generado en build
- [ ] DomainEvent record con primitivos/String ([guía de eventos](domain-events-guide.md))
- [ ] Transformation bidireccional con cifrado de PII registrada
- [ ] EventRoute registrado (topic FQCN + clase destino)
- [ ] Entrada en `EventNameMapping`
- [ ] Tests de roundtrip serialize→deserialize
