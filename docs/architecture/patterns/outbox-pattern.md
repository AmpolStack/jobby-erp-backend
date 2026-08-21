# Outbox Pattern

* Guía educativa · Decisión: [ADR-0024](../adr/adr-0024-outbox-pattern-atomicidad-bd-kafka.md) (estado: propuesta) · Guía operativa: [internal/guides/domain-events-guide.md](../../internal/guides/domain-events-guide.md)

---

## El problema

Con [Domain Events vía pull](domain-events.md), los eventos se publican *después* del commit. Eso garantiza no publicar hechos que no ocurrieron — pero deja una ventana de milisegundos donde el servicio puede caer entre el commit y la publicación. El evento se pierde.

Para un email de bienvenida, tolerable (se reintenta, se ignora). Para un **evento fiscal** ("factura X fue emitida ante la DIAN"), inaceptable: la pérdida rompe auditoría, proyecciones y sincronización con sistemas contables.

Es el problema clásico de **atomicidad entre dos sistemas sin transacción distribuida**: la BD confirma su mitad, Kafka puede no confirmar la suya.

## La idea central

Dejar de intentar "escribir en dos lugares a la vez". Escribir en **uno** (la BD) y dejar que un relay lleve el mensaje al segundo:

```
┌─ Transacción única ────────────────────┐
│  1. Guardar agregado                   │
│  2. Guardar OutboxEvent (mismo commit) │
└────────────────────────────────────────┘
            │
            ▼  relay (@Scheduled o CDC)
      Publicar a Kafka → marcar como enviado
```

El evento está en la BD *antes* de existir en Kafka. Si el servicio cae tras el commit, el relay lo recupera al reiniciar. La garantía pasa de "esperemos que no caiga" a at-least-once estructural.

## El costo que nadie te cuenta: idempotencia

At-least-once significa **duplicados posibles** (el relay publica pero muere antes de marcar). Por eso el patrón obliga a consumers idempotentes:

| Tipo de consumer | Estrategia |
|---|---|
| Crítico (proyecciones fiscales) | Tabla de eventos procesados (`eventId` + estado) |
| Naturalmente idempotente (enviar email) | Sin tabla — reenviar es inocuo |
| Con clave natural | Deduplication key (`ownerId`) |

## Por qué aún no está implementado

Este ADR está en estado `propuesta` deliberadamente ([ADR-0024](../adr/adr-0024-outbox-pattern-atomicidad-bd-kafka.md)): el diseño está cerrado, pero su activación está ligada a `billing-service` ([roadmap](../../project/roadmap.md)) — el primer servicio donde un evento perdido tiene consecuencias legales. Hoy los eventos de user-service son tolerantes a pérdida eventual (retry topics + DLT cubren el caso).

Lecciones de esta decisión:

1. **No todos los eventos merecen outbox** — el costo (colección extra, relay, idempotencia obligatoria) debe justificarse por criticidad.
2. **La infraestructura ya anticipa**: Debezium/CDC está planificado como relay alternativo; el compose ya reserva el espacio.

## Trade-offs honestos

| A favor | En contra |
|---|---|
| Cero pérdida de eventos críticos | Latencia de publicación (intervalo del relay) |
| Auditoría natural de eventos emitidos | Colección outbox con ciclo de vida (purga) |
| Elimina transacciones distribuidas | Obliga idempotencia en todos los consumers críticos |

**Cuándo NO usarlo:** eventos donde la pérdida eventual es aceptable y los reintentos bastan — pagar idempotencia universal por conveniencia es sobre-ingeniería.

## Dónde verlo

- Diseño completo: [ADR-0024](../adr/adr-0024-outbox-pattern-atomicidad-bd-kafka.md)
- Infraestructura anticipada: bloque Debezium comentado en `shared/docker/docker-compose-messaging.yml`
- Activación prevista: Fase 2 del [roadmap](../../project/roadmap.md)

## Para ir más profundo

- Chris Richardson — *Microservices Patterns*, capítulo de messaging reliably
- [ADR-0008](../adr/adr-0008-kafka-kraft-mensajeria.md) — por qué Kafka (log inmutable + replay) hace viable el patrón
