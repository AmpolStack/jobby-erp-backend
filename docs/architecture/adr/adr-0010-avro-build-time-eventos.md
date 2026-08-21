# ADR-0010: Avro generado en build-time para eventos

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

Los eventos de dominio viajan por Kafka ([ADR-0008](adr-0008-kafka-kraft-mensajeria.md)) y necesitan un formato de serialización con evolución controlada: los productores y consumidores se despliegan de forma independiente, por lo que el contrato del mensaje debe versionarse explícitamente y tolerar cambios compatibles.

## Impulsores de la decisión

* Contratos de mensaje versionados y validados contra compatibilidad (backward/forward).
* Rendimiento binario para volúmenes POS.
* Generación de clases tipadas sin mantenimiento manual.
* Integración natural con schema registry ([ADR-0009](adr-0009-apicurio-wire-format-confluent.md)).

## Opciones consideradas

### Opción A: JSON plano con convención de campos

* Bien, porque legible a simple vista y trivial de depurar.
* Malo, porque sin esquema formal no hay verificación de compatibilidad; payload voluminoso; errores de tipo descubiertos en runtime.

### Opción B: Protobuf

* Bien, porque tipado fuerte, evolución robusta, multi-lenguaje excelente.
* Malo, porque su ecosistema Kafka (schema registry + serializadores Spring) es menos nativo que Avro; el proyecto es JVM-only hoy.

### Opción C: Avro con generación en build-time

Esquemas `.avsc` versionados en `src/main/resources/avro`; `avro-maven-plugin` genera POJOs en fase `generate-sources`.

* Bien, porque esquema = contrato verificable; wire format compacto; integración directa con Apicurio; las clases generadas son inmutables y tipadas.
* Malo, porque exige disciplina en evolución de esquemas (solo cambios compatibles); las clases generadas no deben editarse a mano.

## Decisión

Opción elegida: **C — Avro build-time**. Configuración: `avro-maven-plugin 1.12.0`, `stringType=String` (evita `CharSequence` de Avro), package destino `com.jobby.<servicio>.events`. Ejemplo vivo: `created-user-event.avsc` → `UserCreatedSchema`.

La conversión entre el evento de dominio y la clase Avro **no** usa MapStruct sino transformaciones explícitas con `Result` ([ver ADR-0021](adr-0021-contratos-records-application.md) y [guía de mensajería](../../internal/guides/messaging-guide.md)): cada evento tiene su `Transformation<DominioEvent, AvroSchema>` bidireccional registrada en `TransformationRegistry`.

Nota de privacidad conectada: los campos PII de los esquemas se declaran tipo `bytes` porque viajan cifrados ([ADR-0029](adr-0029-pii-cifrada-end-to-end-kafka.md)).

### Justificación

Avro es el estándar de facto del ecosistema Kafka/JVM con soporte first-party en Apicurio. La generación build-time elimina toda sincronización manual entre esquema y código: si el `.avsc` cambia incompatible, el build o el registry lo rechazan antes que producción.

## Consecuencias

* **Positivas**: contratos verificables; payload compacto; clases generadas consistentes; auto-registro de esquemas en despliegue.
* **Negativas**: doble representación por evento (dominio ↔ Avro) con transformación explícita que mantener; curva de reglas de compatibilidad Avro.
* **Neutras**: la dualidad dominio/Avro es deliberada — el dominio expresa hechos de negocio; Avro es un detalle de transporte ([ADR-0023](adr-0023-separacion-event-interno-domainevent-externo.md)).

## Validación

Tests de roundtrip por esquema (`serialize → deserialize → igualdad semántica`) como parte de la suite estándar ([ADR-0037](adr-0037-convenciones-tests.md)).

## Referencias

* [Guía interna de mensajería](../../internal/guides/messaging-guide.md)
* [ADR-0009](adr-0009-apicurio-wire-format-confluent.md) · [ADR-0022](adr-0022-eventos-dominio-pull-domain-events.md)
