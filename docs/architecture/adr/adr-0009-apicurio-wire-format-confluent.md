# ADR-0009: Apicurio Registry con wire format compatible Confluent

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

Kafka serializa eventos con Avro ([ADR-0010](adr-0010-avro-build-time-eventos.md)), lo que exige un schema registry para versionar esquemas y resolverlos en runtime. La decisión cubre dos preguntas: *qué registry* y *con qué wire format* — es decir, cómo se codifica el ID de esquema dentro del mensaje binario.

El detonante concreto: el wire format nativo de Apicurio (magic byte + ID de 8 bytes) no era deserializable por las herramientas estándar de inspección (kafbat/Kafka UI, AKHQ), que esperan el formato Confluent de facto (magic byte + ID de 4 bytes). Sin inspección visual de mensajes, operar y depurar la mensajería era ciego.

## Impulsores de la decisión

* Open source puro: evitar licenciamiento restrictivo del Confluent Schema Registry.
* Inspeccionabilidad: poder ver mensajes desde kafbat sin deserializadores custom.
* Compatibilidad con el ecosistema: herramientas que asumen formato Confluent deben funcionar.
* Costo por mensaje mínimo (el ID viaja en cada mensaje).

## Opciones consideradas

### Opción A: Confluent Schema Registry

* Bien, porque es el estándar de facto con tooling universal.
* Malo, porque su licencia comunitaria restringe uso con otros brokers/proveedores; contradice la filosofía open source del proyecto.

### Opción B: Apicurio con modo "headers only"

El ID de esquema viaja solo en headers de Kafka, payload limpio.

* Bien, porque payload 100% Avro puro.
* Malo, porque es un formato privativo sin soporte en herramientas externas: exactamente el problema que motivó esta decisión.

### Opción C: Apicurio con wire format compatible Confluent

Apicurio configurado para emitir `[0x00][4B contentId][Avro]` — idéntico al formato Confluent — usando `use-id: contentId` + `Legacy4ByteIdHandler`, con auto-registro de esquemas activado.

* Bien, porque registry 100% open source **y** compatibilidad total con herramientas del ecosistema; `contentId` resuelve vía endpoint ccompat `/schemas/ids/{id}`.
* Malo, porque requiere configuración asimétrica producer/consumer y un serializador propio ([ver decisión](#decisión)).

## Decisión

Opción elegida: **C**. Apicurio Registry 3.x con storage `kafkasql` (persistencia sobre el propio clúster Kafka — topics internos `kafkasql-journal`/`kafkasql-snapshots` con RF=3), expuesto junto a kafbat para inspección.

Configuración asimétrica:

| Rol | Propiedades clave |
|---|---|
| Producer | `DualAvroKafkaSerializer` propio: escribe magic byte + contentId (4B) en payload **y** metadatos en headers (`eventId`, `occurredOn`, `aggregateType`, `operationType`) |
| Consumer | `AvroKafkaDeserializer` estándar con `apicurio.registry.headers.enabled: false` (lee el ID del payload) |

Ambos con `use-id: contentId` + `Legacy4ByteIdHandler`. El serializador dual vive en `shared/serde/DualAvroKafkaSerializer.java`: extiende `AvroKafkaSerializer` de Apicurio, escribe el ID en payload (para herramientas externas) y delega los headers al `headersHandler` (para visibilidad de metadatos en UI). El consumer funciona sin cambios porque ignora headers.

Existe plan de rollback documentado de 6 pasos (serializer → propiedades YAML → métodos de metadata → renombres) ante cualquier regresión.

### Justificación

Obtiene simultáneamente las tres cosas que parecían excluyentes: licencia abierta (Apicurio), ecosistema compatible (wire format Confluent) y metadatos visibles (headers adicionales). El costo es un serializador propio de ~40 líneas, perfectamente alineado con *explicitness over magic* ([ADR-0039](adr-0039-principio-explicitness-over-magic.md)).

## Consecuencias

* **Positivas**: mensajes inspeccionables en kafbat/AKHQ sin código custom; migración futura a herramientas Confluent-compatible trivial; storage del registry sin componente adicional.
* **Negativas**: configuración asimétrica producer/consumer que debe documentarse bien (hecho en [guía de mensajería](../../internal/guides/messaging-guide.md)); dependencia del comportamiento legacy de Apicurio.
* **Neutras**: para cientos de esquemas, 4 bytes de ID son más que suficientes.

## Validación

Tests de roundtrip Avro↔evento ([ADR-0037](adr-0037-convenciones-tests.md)); verificación manual en kafbat de deserialización y headers; observaciones `kafka.publish` con tag de topic.

## Referencias

* [Guía interna de mensajería](../../internal/guides/messaging-guide.md)
* [ADR-0008](adr-0008-kafka-kraft-mensajeria.md) · [ADR-0010](adr-0010-avro-build-time-eventos.md) · [ADR-0029](adr-0029-pii-cifrada-end-to-end-kafka.md)
