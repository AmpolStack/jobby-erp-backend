# ADR-0029: PII cifrada extremo a extremo en mensajes Kafka

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

Los eventos de dominio viajan por Kafka ([ADR-0008](adr-0008-kafka-kraft-mensajeria.md)) y contienen PII: un `UserCreatedEvent` lleva nombre y email del usuario. Si esos campos viajaran en claro, cualquier lector del topic (admins del broker, herramientas de inspección como kafbat, dumps de mensajería) vería datos personales — el mismo vector que [ADR-0028](adr-0028-cifrado-campo-indice-hmac-searchable.md) cerró en la base de datos, abierto en el bus.

## Impulsores de la decisión

* Coherencia total: la PII debe estar protegida en **todos** los medios de persistencia/tránsito (BD, bus, logs).
* El broker no debe ser confiable para contenido sensible.
* Sin dependencia de configuración de cifrado del broker (TLS protege tránsito, no reposo en topics).

## Opciones consideradas

### Opción A: Enviar PII en claro + TLS + permisos del broker

* Bien, porque simplicidad total.
* Malo, porque TLS protege el cable, no el mensaje en reposo; kafbat/lectores legítimos ven todo; un misconfigured ACL expone la base completa de usuarios.

### Opción B: No enviar PII — solo IDs, y lookup a la BD destino

* Bien, porque el mensaje no contiene nada sensible.
* Malo, porque acopla consumers a la BD del productor (viola Database per Service [ADR-0002](adr-0002-microservicios-con-database-per-service.md)) o exige llamadas síncronas extra; el evento deja de ser autocontenido.

### Opción C: Cifrar los campos PII dentro del payload antes de serializar

Los esquemas Avro declaran los campos sensibles tipo `bytes`; la transformación dominio→Avro cifra con `EncryptionService` (el mismo AES-GCM de [ADR-0028](adr-0028-cifrado-campo-indice-hmac-searchable.md)) antes de serializar. La transformación inversa descifra al consumir.

* Bien, porque el evento sigue siendo autocontenido (B sin sus desventajas); el broker solo ve bytes opacos; reutiliza exactamente la infraestructura criptográfica existente.
* Malo, porque las transformaciones deben recordar cifrar cada campo nuevo (mitigado: convención + review + skill); sin búsqueda sobre campos cifrados en consumers (no requerida hoy).

## Decisión

Opción elegida: **C**. Implementación viva: `created-user-event.avsc` declara `name`/`email` como `bytes`; `CreatedUserEventToCreatedUserAvro` cifra ambos campos con `EncryptionService` antes de serializar. Los metadatos no sensibles viajan como headers ([ADR-0009](adr-0009-apicurio-wire-format-confluent.md)): `eventId`, `occurredOn`, `aggregateType`, `operationType`.

Reglas:

1. Todo campo PII en esquemas Avro es `bytes` cifrado — nunca `string` en claro.
2. El cifrado ocurre en la Transformation (infraestructura), nunca en el dominio.
3. Las claves son las mismas del mecanismo de campo ([ADR-0028](adr-0028-cifrado-campo-indice-hmac-searchable.md)) — una sola política de claves.

### Justificación

Extiende la garantía de confidencialidad al último medio donde la PII descansa. Es barato (reusa AES-GCM y puertos existentes) y cierra el triángulo completo: BD cifrada, bus cifrado, logs sanitizados ([ADR-0030](adr-0030-sanitizacion-errores-support-id.md)).

## Consecuencias

* **Positivas**: privacidad end-to-end verificable; inspección segura de topics (se ve estructura, no datos); coherencia criptográfica única.
* **Negativas**: mensajes ligeramente mayores (ciphertext > plaintext); depuración requiere descifrar conscientemente.
* **Neutras**: compatible con el wire format Confluent ([ADR-0009](adr-0009-apicurio-wire-format-confluent.md)) — el cifrado es ortogonal a la serialización.

## Validación

Tests de roundtrip transformación↔transformación inversa verifican cifrado real del payload; revisión manual en kafbat confirma opacidad; skill `security-sensitive-data` planificado cubre nuevos eventos.

## Referencias

* [Guía interna de seguridad](../../internal/guides/security-guide.md) · [Patrón público: Searchable Encryption](../patterns/searchable-encryption.md)
