# ADR-0007: Java 21 + Spring Boot como framework por defecto

* Fecha: 2026-08-20 (retro-documentada)
* Estado: aceptada

## Contexto y problema

Cada microservicio necesita un framework base. El README del proyecto contempla desde el inicio un stack multi-framework: Spring Boot por defecto, con Quarkus/Micronaut para servicios con requisitos reactivos o de arranque frío específicos. Había que fijar la línea base y el criterio de desviación.

## Impulsores de la decisión

* Ecosistema maduro para todo lo que el producto necesita: Kafka, MongoDB, Redis, observabilidad, seguridad.
* Talento local abundante en Java/Spring (Colombia/LATAM) — relevante para adopción open source.
* Java 21 LTS: records, sealed interfaces, pattern matching — pilares del estilo de código del proyecto (Result como sealed interface, contratos como records).
* Objetivo educativo: Spring Boot es el denominador común que más desarrolladores pueden estudiar.

## Opciones consideradas

### Opción A: Quarkus/Micronaut para todos

* Bien, porque arranque rápido, huella baja, first-class native compilation.
* Malo, porque ecosistema de integraciones más pequeño; curva extra para contribuyentes; sin evidencia aún de que el perfil runtime lo justifique globalmente.

### Opción B: Spring Boot para todos, sin excepciones jamás

* Bien, porque homogeneidad total.
* Malo, porque descarta ventajas reales de runtimes alternativos donde existan (ej. workers de alta concurrencia, serverless esporádico).

### Opción C: Spring Boot por defecto + criterio explícito de desviación

Spring Boot 3.5.x / Java 21 como base; Quarkus o Micronaut solo cuando un servicio demuestre requisito específico (reactividad intensiva, arranque frío, memoria) que su runtime resuelva mejor — documentado en ADR propio.

* Bien, porque consistencia por defecto con escape hatch racional y trazable.
* Malo, porque admite heterogeneidad controlada (costo cognitivo acotado por la regla).

## Decisión

Opción elegida: **C**. Base: Java 21 + Spring Boot 3.5.5 (BOM importado en POM raíz). Ningún servicio usa otro framework hoy; las desviaciones futuras exigirán ADR con evidencia de perfil runtime.

Detalles de build relevantes:

- `maven-compiler-plugin` con cadena de annotation processors ordenada: mapstruct-processor → lombok → lombok-mapstruct-binding.
- Surefire con `-XX:+EnableDynamicAgentLoading` (requisito de Mockito en JDK 21+).
- `<parameters>true</parameters>` para nombres de parámetros reflejables.

### Justificación

La regla "default + excepción justificada" evita tanto la homogeneidad dogmática como la fragmentación caprichosa. Cada desviación costará un ADR — exactamente el nivel de fricción correcto.

## Consecuencias

* **Positivas**: velocidad de desarrollo alta; integraciones first-party (Kafka, Micrometer, security); talento disponible; features de lenguaje 21 aprovechadas a fondo.
* **Negativas**: arranque/memoria superiores a Quarkus en escenarios serverless (no es nuestro perfil actual); dependencia del ciclo de Spring.
* **Neutras**: la versión se gestiona centralizada en el POM raíz del monorepo ([ADR-0004](adr-0004-monorepo-maven-multi-modulo.md)).

## Validación

El POM raíz fija versiones vía `dependencyManagement`; cualquier servicio nuevo hereda la base sin configuración adicional. Desviaciones visibles en review al instante.

## Referencias

* [README — Tech Stack](../../../README.md)
* [ADR-0004](adr-0004-monorepo-maven-multi-modulo.md) — gestión centralizada de versiones
