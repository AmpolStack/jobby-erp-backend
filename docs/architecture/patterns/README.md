# Patrones en Jobby ERP

Guías educativas que explican cada patrón aplicado **en el contexto real de este codebase**: cómo funciona, por qué lo elegimos, sus trade-offs y dónde verlo en el código.

Estas guías complementan a los [ADRs](../adr/README.md): el ADR registra *qué se decidió y qué alternativas se descartaron*; la guía explica *cómo funciona el patrón y por qué vale la pena aprenderlo*. Son también el material base de la [serie de artículos](../../articles/README.md).

---

## Guías disponibles

| Guía | Patrón | ADR principal |
|---|---|---|
| [Hexagonal + DDD con dominio puro](hexagonal-ddd.md) | Arquitectura | [ADR-0003](../adr/adr-0003-ddd-hexagonal-dominio-puro.md) |
| [Result Pattern](result-pattern.md) | Manejo de errores | [ADR-0016](../adr/adr-0016-result-pattern-unico.md) |
| [CQRS y buses in-process](cqrs-buses.md) | Arquitectura / despacho | [ADR-0006](../adr/adr-0006-cqrs-parcial.md), [ADR-0020](../adr/adr-0020-buses-in-process-command-query-event.md) |
| [Domain Events](domain-events.md) | Eventos de dominio | [ADR-0022](../adr/adr-0022-eventos-dominio-pull-domain-events.md), [ADR-0023](../adr/adr-0023-separacion-event-interno-domainevent-externo.md) |
| [Outbox Pattern](outbox-pattern.md) | Consistencia BD-mensajería | [ADR-0024](../adr/adr-0024-outbox-pattern-atomicidad-bd-kafka.md) |
| [Searchable Encryption](searchable-encryption.md) | Seguridad de datos | [ADR-0028](../adr/adr-0028-cifrado-campo-indice-hmac-searchable.md), [ADR-0029](../adr/adr-0029-pii-cifrada-end-to-end-kafka.md) |

## Ruta de aprendizaje sugerida

1. **[Hexagonal + DDD](hexagonal-ddd.md)** — el esqueleto sobre el que todo lo demás vive.
2. **[Result Pattern](result-pattern.md)** — el estilo transversal que unifica todo flujo.
3. **[CQRS y buses](cqrs-buses.md)** — cómo viajan las intenciones dentro del servicio.
4. **[Domain Events](domain-events.md)** — cómo el dominio declara hechos y el mundo reacciona.
5. **[Searchable Encryption](searchable-encryption.md)** — cómo protegemos PII sin sacrificar búsqueda.
6. **[Outbox Pattern](outbox-pattern.md)** — cómo garantizaremos entrega cuando los eventos sean fiscales.

## Cómo leer cada guía

Cada una sigue la misma estructura:

- **El problema** — qué duele sin el patrón.
- **El patrón** — idea central explicada con código real del repo.
- **Trade-offs honestos** — qué cuesta, cuándo NO usarlo.
- **Dónde verlo** — rutas exactas de archivos.
- **Para ir más profundo** — ADRs, guías internas y referencias externas.
