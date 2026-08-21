# Principios del Proyecto

* Tipo: documento de formalización · Audiencia: todas · Última actualización: 2026-08-20

Los principios son las reglas culturales que gobiernan cada decisión técnica. Cuando un ADR o una guía interna no cubre un caso nuevo, estos principios son el criterio de desempate.

---

## 1. Explicitness over magic

Preferimos código explícito y verificable sobre automatismos ocultos. Registro manual de handlers en lugar de reflexión; ordeñe manual de eventos en lugar de AOP; composición visible de decoradores en lugar de proxies transparentes.

> Si algo falla, debe poder rastrearse leyendo código, no adivinando qué hizo el framework por debajo.

* Formalizado en [ADR-0039](../architecture/adr/adr-0039-principio-explicitness-over-magic.md).

## 2. Dominio puro, dependencias hacia adentro

El paquete `domain/` no importa nada de infraestructura ni de Spring. Las dependencias apuntan siempre hacia adentro (hexagonal). Una violación es deuda técnica automática, verificable en code review.

* Formalizado en [ADR-0003](../architecture/adr/adr-0003-ddd-hexagonal-dominio-puro.md) · Reglas operativas en [guía de arquitectura](../internal/guides/architecture-rules.md).

## 3. Zero exceptions para reglas de negocio

Ninguna regla de negocio se comunica lanzando excepciones. Todo flujo de negocio retorna `Result<T, Error>` explícito y tipado. Las excepciones quedan reservadas para fallos de infraestructura y bugs de programación.

* Formalizado en [ADR-0016](../architecture/adr/adr-0016-result-pattern-unico.md).

## 4. Cada validación vive exactamente en una capa

Si el mismo chequeo aparece en dos capas, uno sobra. Config valida propiedades (fail-fast en startup), Value Object valida reglas intrínsecas, aggregate valida consistencia interna, caso de uso valida reglas con repositorios, controller valida estructura HTTP.

* Formalizado en [ADR-0027](../architecture/adr/adr-0027-validacion-distribuida-por-capas.md).

## 5. Eventos después de la transacción

Los eventos de dominio jamás se publican dentro de una transacción de BD. Se publican solo después de que la transacción confirma; si la transacción falla, los eventos nunca existieron para el mundo exterior.

* Formalizado en [ADR-0022](../architecture/adr/adr-0022-eventos-dominio-pull-domain-events.md) y [ADR-0025](../architecture/adr/adr-0025-transacciones-explicitas-transaction-orchestrator.md).

## 6. Nunca texto plano para datos sensibles

Datos personales operativos (email, teléfono, identificación) viajan y descansan cifrados. La búsqueda sobre datos cifrados se resuelve con índices HMAC deterministas, no sacrificando privacidad.

* Formalizado en [ADR-0028](../architecture/adr/adr-0028-cifrado-campo-indice-hmac-searchable.md) y [ADR-0029](../architecture/adr/adr-0029-pii-cifrada-end-to-end-kafka.md).

## 7. Flexibilidad pragmática sobre dogma

Las reglas anteriores son inviolables; todo lo demás admite excepciones justificadas y documentadas:

- Duplicar un VO entre bounded contexts es preferible a forzar releases coordinados.
- Monorepo mientras el equipo sea pequeño; separar cuando haya ciclos de release independientes.
- MapStruct para mapeo estándar; mapeo manual cuando la lógica no cabe en anotaciones.
- Granularidad de repositorios según necesidad real, no según receta.
- `@Transactional` declarativo donde no se requiere control fino.

## 8. Documentar para aprender

Cada patrón aplicado produce documentación que explica cómo funciona, por qué se eligió, sus trade-offs y cómo se manifiesta en este codebase. Una decisión sin ADR es una decisión perdida.

## 9. Open source construido en público

El desarrollo es público desde el día uno. Errores, refactors y decisiones forman parte del relato — también son contenido educativo (ver [estrategia de contenidos](../articles/content-strategy.md)).
