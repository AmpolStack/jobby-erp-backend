# ADR-0032: Nomenclatura FQCN invertido para topics y consumer groups

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

Con múltiples servicios produciendo y consumiendo eventos sobre Kafka ([ADR-0008](adr-0008-kafka-kraft-mensajeria.md)), los nombres de topics y consumer groups necesitan una convención que evite colisiones, revele ownership a simple vista y sea ordenable en las herramientas de inspección (kafbat lista alfabéticamente).

## Impulsores de la decisión

* Ownership visible: ¿quién produce/consume este topic? debe leerse en el nombre.
* Colisiones imposibles entre servicios y entre ambientes.
* Agrupación natural por servicio en UIs de inspección.
* Trazabilidad directa nombre ↔ código fuente.

## Opciones consideradas

### Opción A: Nombres cortos funcionales (`user-created`, `email-change`)

* Bien, porque legibles y compactos.
* Malo, porque colisionan trivialmente entre servicios/ambientes; no revelan ownership; se dispersan alfabéticamente en la UI.

### Opción B: Prefijo de ambiente + funcional (`prod-user-created`)

* Bien, porque distingue ambientes.
* Malo, porque sigue sin ownership; el ambiente ya suele estar resuelto por clúster/namespace.

### Opción C: FQCN invertido — `com.jobby.<servicio>.<acción>`

Topics: `com.jobby.user.create-user`, `com.jobby.user.email-change-request`.
Consumer groups: `com.jobby.user-service.<acción>-consumer` (ej. `com.jobby.user-service.create-user-consumer`).

* Bien, porque ownership explícito; agrupación alfabética perfecta por servicio (`com.jobby.user.*` juntas); colisión imposible si el paquete base es único; correspondencia directa con el package Java.
* Malo, porque nombres largos (aceptado: la longitud compra trazabilidad).

## Decisión

Opción elegida: **C**. Reglas:

1. Topic = `com.jobby.<servicio-productor>.<acción-en-kebab-case>`.
2. Consumer group = `com.jobby.<servicio-consumidor>.<acción>-consumer`.
3. El mapping clase→nombre vive exclusivamente en `EventNameMapping` ([ADR-0023](adr-0023-separacion-event-interno-domainevent-externo.md)) — el dominio jamás menciona topics.

### Justificación

La convención convierte el catálogo de topics en un índice organizado del sistema: filtrar `com.jobby.user.` en kafbat muestra todo el universo de eventos del user-service. La correspondencia package↔topic hace el mapeo mental trivial para cualquier desarrollador.

## Consecuencias

* **Positivas**: ownership evidente; cero colisiones; inspección agrupada; naming determinista sin discusiones.
* **Negativas**: nombres largos en logs/métricas (tags de baja cardinalidad los manejan bien).
* **Neutras**: renombrar un topic es operación de infraestructura (nuevo nombre + migración de consumers), nunca de dominio.

## Validación

Review verifica la convención en cada topic nuevo; verificación visual periódica en kafbat; documentado en [guía de mensajería](../../internal/guides/messaging-guide.md).

## Referencias

* [ADR-0009](adr-0009-apicurio-wire-format-confluent.md) · [ADR-0023](adr-0023-separacion-event-interno-domainevent-externo.md)
