# ADR-0024: Outbox Pattern para atomicidad BD-Kafka

* Fecha: 2026-08-20
* Estado: propuesta

## Contexto y problema

Con `pullDomainEvents()` ([ADR-0022](adr-0022-eventos-dominio-pull-domain-events.md)), los eventos se publican *después* de la transacción exitosa. Eso garantiza no publicar sobre estados que no ocurrieron, pero deja una ventana residual: si el servicio cae entre el commit y la publicación, el evento se pierde. Para emails de bienvenida es tolerable; para eventos fiscales (factura emitida) es inaceptable.

El diseño original ya identificó esto y definió el patrón: persistir el evento en una colección `outbox_events` **dentro de la misma transacción** de negocio, y un relay posterior lo publica a Kafka y lo marca.

## Impulsores de la decisión

* Garantía at-least-once de entrega de eventos críticos (fase billing).
* Eliminar la ventana de pérdida entre commit y publish.
* Base para reprocesos y auditoría de eventos emitidos.
* La infraestructura ya anticipa CDC (Debezium planificado en compose) como relay alternativo.

## Opciones consideradas

### Opción A: Mantener publicación post-commit simple

* Bien, porque cero infraestructura adicional; suficiente para efectos secundarios tolerantes.
* Malo, porque ventana de pérdida real ante crash; insuficiente como garantía fiscal.

### Opción B: Transacciones distribuidas (2PC / XA)

* Bien, porque atomicidad "clásica" entre BD y broker.
* Malo, porque Kafka no soporta XA; latencia y bloqueo; complejidad operativa injustificable en microservicios modernos.

### Opción C: Outbox Pattern con relay

1. El caso de uso persiste `OutboxEvent` en MongoDB **en la misma transacción** del agregado.
2. Relay publica: `@Scheduled(fixedDelay=1000)` que consulta pendientes, publica vía `MessagingPublisher` y marca como enviado — o Debezium/CDC leyendo el oplog (evaluado en Fase 5 del [roadmap](../../project/roadmap.md)).
3. Consumers idempotentes por tabla de eventos procesados (`eventId` + estado) para tolerar duplicados del at-least-once.

* Bien, porque atomicidad real sin 2PC; auditoría natural de eventos; relay desacoplado del request path.
* Malo, porque colección adicional con ciclo de vida propio (purga); latencia de publicación (hasta el intervalo del relay); obliga a idempotencia en todos los consumers críticos.

## Decisión

Opción elegida: **C — Outbox Pattern**, activación por criticidad:

- **Fase actual (user-service)**: se mantiene publicación post-commit directa — los eventos actuales (bienvenida, cambio de email) son reintentables y tolerantes a pérdida eventual (retry topics + DLT ya configurados).
- **Fase billing ([roadmap](../../project/roadmap.md))**: outbox obligatorio para todo evento fiscal.

Estrategias de idempotencia definidas para consumers: tabla eventId+estado en BD para handlers críticos; eventos naturalmente idempotentes (emails) sin tabla; deduplication key por `ownerId` donde aplique.

### Justificación

Es estado `propuesta` porque su implementación completa está ligada al núcleo de facturación, donde su costo se justifica plenamente. El diseño está cerrado; falta implementarlo cuando el primer evento fiscal exista. Anticipar el patrón ahora evita rediseños futuros.

## Consecuencias

* **Positivas** (al activarse): cero pérdida de eventos fiscales; auditoría de emisión; base para proyecciones confiables.
* **Negativas**: colección outbox con purga programada; consumers deben ser idempotentes desde el día uno.
* **Neutras**: convive con publicación directa para eventos no críticos.

## Validación

Al implementarse: tests de crash-simulación (commit sin publish → relay recupera), métricas de lag del relay, alertas sobre outbox acumulado.

## Referencias

* [Patrón público: Outbox](../patterns/outbox-pattern.md)
* [ADR-0008](adr-0008-kafka-kraft-mensajeria.md) · [ADR-0022](adr-0022-eventos-dominio-pull-domain-events.md)
