# ADR-0008: Kafka 4.0 en modo KRaft como bus de mensajería

* Fecha: 2026-08-20 (retro-documentada)
* Estado: aceptada

## Contexto y problema

Con microservicios y Database per Service ([ADR-0002](adr-0002-microservicios-con-database-per-service.md)), la comunicación asíncrona por eventos es el mecanismo principal de desacoplamiento. Había que elegir el broker de mensajería que soportaría los eventos de dominio entre servicios, con requisitos de durabilidad propios del dominio fiscal: un evento de factura emitida no puede perderse.

## Impulsores de la decisión

* Durabilidad y replay: los eventos deben poder re-leerse (reprocesos, auditoría, reconstrucción de proyecciones).
* Throughput para picos POS y para ingesta analítica futura.
* Ecosistema de schema registry y herramientas de inspección.
* Operación local simple para contribuyentes (Docker Compose).

## Opciones consideradas

### Opción A: RabbitMQ

Broker de mensajería con routing flexible (exchanges, colas).

* Bien, porque routing sofisticado, mensajes por push con baja latencia, operación sencilla.
* Malo, porque replay limitado (mensajes se descartan al consumirse), throughput menor en escenarios de log masivo, y el patrón event-sourcing/proyecciones queda contra el grano.

### Opción B: Kafka 4.0 en modo KRaft

Log distribuido inmutable; sin ZooKeeper (KRaft reemplaza el consenso externo).

* Bien, porque persistencia y replay nativos; throughput alto; orden garantizado por partición; ecosistema ideal (Apicurio, kafbat, Debezium); despliegue KRaft simplifica la infraestructura local a 1 controlador + 3 brokers.
* Malo, porque operación más pesada que un broker simple; particiones requieren diseño de keys; no apto para routing complejo out-of-the-box.

### Opción C: Cloud-managed (SQS/PubSub u otros)

* Bien, porque cero operación.
* Malo, porque ata el proyecto open source a un proveedor; contradice la filosofía self-hosted del stack de observabilidad ([ADR-0013](adr-0013-observabilidad-self-hosted.md)).

## Decisión

Opción elegida: **B — Kafka 4.0 KRaft**. Infraestructura local: 1 controlador + 3 brokers con healthchecks de quórum (`kafka-metadata-quorum.sh`), redes Docker nombradas (`jobby-messaging-network`) para que cualquier servicio futuro se conecte.

Configuración base de topics de negocio:

- Replication factor 3, `min.insync.replicas=2`, producer idempotente con `acks=all`.
- Consumer con commit manual y `auto-offset-reset: earliest`.
- Reintentos con `@RetryableTopic` (4 intentos, backoff exponencial 2s × 2.0) + DLT `.DLT` para inspección manual.

### Justificación

El dominio fiscal exige que "el evento nunca se pierda" sea una propiedad del sistema, no una esperanza. El log inmutable de Kafka + idempotencia + DLT da exactamente esa semántica. KRaft elimina ZooKeeper, reduciendo la fricción de levantar el entorno completo — crítico para contribuyentes.

## Consecuencias

* **Positivas**: replay y auditoría nativos; base lista para Outbox/CDC ([ADR-0024](adr-0024-outbox-pattern-atomicidad-bd-kafka.md)); tooling maduro de inspección (kafbat).
* **Negativas**: operación más compleja que RabbitMQ; obliga a diseñar keys de partición por flujo; consumo requiere manejo explícito de offsets.
* **Neutras**: la nomenclatura de topics sigue convención propia ([ADR-0032](adr-0032-nomenclatura-fqcn-topics-consumer-groups.md)).

## Validación

Healthchecks de quórum en compose; tests de roundtrip Avro ([ADR-0037](adr-0037-convenciones-tests.md)); métricas `kafka.publish`/`kafka.publish-async` instrumentadas ([ADR-0013](adr-0013-observabilidad-self-hosted.md)).

## Referencias

* [Guía interna de mensajería](../../internal/guides/messaging-guide.md)
* [ADR-0009](adr-0009-apicurio-wire-format-confluent.md) · [ADR-0024](adr-0024-outbox-pattern-atomicidad-bd-kafka.md)
