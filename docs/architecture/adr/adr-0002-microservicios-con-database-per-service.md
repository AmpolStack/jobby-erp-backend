# ADR-0002: Microservicios con Database per Service

* Fecha: 2026-08-20 (retro-documentada)
* Estado: aceptada

## Contexto y problema

Jobby ERP nace para resolver un problema recurrente del mercado colombiano de facturación: soluciones monolíticas que colapsan bajo carga y se caen justo cuando el negocio más las necesita (fin de mes, temporadas altas). La plataforma debe soportar facturación clásica y POS con alta disponibilidad, y evolucionar módulo a módulo (facturación → notificaciones → analítica) sin que un fallo en un dominio tumbe los demás.

Había que decidir la unidad de despliegue y de datos desde el primer servicio.

## Impulsores de la decisión

* Aislamiento de fallas: la caída de notificaciones no debe impedir emitir facturas.
* Escalado independiente: POS tiene picos de carga radicalmente distintos a reportes.
* Dominios con ciclos de cambio distintos (normativa DIAN vs UX de caja).
* Cumplimiento: los flujos fiscales exigen trazabilidad y consistencia propias.

## Opciones consideradas

### Opción A: Monolito modular

Un solo despliegue con módulos internos bien delimitados.

* Bien, porque simplifica operación inicial, transacciones locales y debugging.
* Malo, porque reproduce exactamente el problema que el proyecto ataca: acoplamiento operativo, escalado global, y fronteras modulares que se erosionan con el tiempo.

### Opción B: Microservicios con base de datos compartida

Varios servicios desplegables contra una misma BD.

* Bien, porque facilita joins entre dominios y transacciones "simples".
* Malo, porque la BD compartida es el acoplamiento más difícil de romper: cambios de esquema coordinados, contención, y límites de servicio que se vuelven cosméticos.

### Opción C: Microservicios con Database per Service

Servicios independientes, cada uno dueño exclusivo de su esquema; comunicación REST donde la latencia importa y eventos asíncronos donde prima el desacoplamiento.

* Bien, porque aísla fallas y carga, permite elegir persistencia por dominio, y hace los límites de servicio reales (no hay forma de hacer join ilegal).
* Malo, porque elimina transacciones distribuidas "gratis": exige patrones de consistencia eventual (eventos, outbox) y duplicación controlada de datos de referencia.

## Decisión

Opción elegida: **C — Microservicios con Database per Service**. Cada servicio posee su BD; ninguna compartida. Comunicación síncrona vía REST para lecturas sensibles a latencia, asíncrona vía eventos Kafka para flujos de desacoplamiento (notificaciones, ingesta analítica).

### Justificación

La propuesta de valor central del producto es "diseñado para no caerse nunca". Eso exige aislamiento de fallas real, que solo es posible si los límites de despliegue y de datos coinciden. El costo de consistencia eventual se asume conscientemente y se mitiga con eventos de dominio ([ADR-0022](adr-0022-eventos-dominio-pull-domain-events.md)) y Outbox ([ADR-0024](adr-0024-outbox-pattern-atomicidad-bd-kafka.md)).

## Consecuencias

* **Positivas**: fallas contenidas por servicio; escalado selectivo; libertad tecnológica por dominio; límites arquitectónicos verificables.
* **Negativas**: sin transacciones entre servicios; requiere Outbox, reintentos e idempotencia; consultas cross-dominio requieren APIs o proyecciones.
* **Neutras**: cada servicio elige su motor (MongoDB operacional hoy; otros motores evaluables por dominio).

## Validación

Los diagramas C1/C2 documentan los límites; cada nuevo servicio exige su propio esquema y su compose independiente. La regla "no shared databases" está escrita en [`CONTRIBUTING.md`](../../../CONTRIBUTING.md).

## Referencias

* [Alcance y servicios](../../project/scope.md)
* [ADR-0008](adr-0008-kafka-kraft-mensajeria.md) — mensajería entre servicios
