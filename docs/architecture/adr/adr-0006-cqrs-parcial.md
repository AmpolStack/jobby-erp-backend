# ADR-0006: CQRS parcial — commands y queries separados, mismo modelo de dominio

* Fecha: 2026-08-20 (retro-documentada)
* Estado: aceptada

## Contexto y problema

Los flujos de escritura (crear owner, cambiar email) y de lectura (consultar owner) tienen requisitos opuestos: la escritura exige invariantes y validación estricta; la lectura exige velocidad y flexibilidad. Había que decidir cuánto CQRS adoptar desde el inicio, sin sobre-ingeniería para un producto que aún no tiene carga real.

Un análisis del codebase temprano (`cqrs-events-analysis`, hoy absorbido en este ADR) detectó además un desbalance: 8 commands vs 1 query, señal de que el sistema era predominantemente transaccional.

## Impulsores de la decisión

* Separar intención: un Command no es lo mismo que una Query ni debe compartir contrato.
* Evitar sobre-ingeniería: proyecciones separadas tienen costo operativo real (sincronización, consistencia eventual de lecturas).
* Mantener abierta la puerta a CQRS completo si las métricas de lectura lo exigen.

## Opciones consideradas

### Opción A: CRUD directo sin separación

Métodos `save/find` genéricos por entidad.

* Bien, porque mínimo esfuerzo.
* Malo, porque pierde la semántica de negocio (¿qué significa "confirmar cambio de email"?) y mezcla responsabilidades de lectura/escritura en los mismos puertos.

### Opción B: CQRS completo con modelos separados

Modelo de escritura (agregados) + proyecciones de lectura desnormalizadas sincronizadas por eventos.

* Bien, porque lecturas ultra-rápidas modeladas por caso de uso.
* Malo, porque introduce consistencia eventual en lecturas, infraestructura de sincronización y doble mantenimiento *antes de tener evidencia de que la lectura es cuello de botella*.

### Opción C: CQRS parcial (CQS a nivel de contratos)

Commands y Queries como records separados en paquetes distintos (`application/contracts/commands` / `.../queries`), despachados por buses in-process ([ADR-0020](adr-0020-buses-in-process-command-query-event.md)), pero **mismo modelo de dominio** para leer y escribir.

* Bien, porque captura el 80% del valor (claridad de intención, handlers enfocados, testabilidad) con 20% del costo; evolución natural a CQRS completo cuando haya datos que lo justifiquen.
* Malo, porque lecturas complejas atraviesan agregados completos (overfetching potencial hasta tener proyecciones).

## Decisión

Opción elegida: **C — CQRS parcial**. Commands y queries separados como contratos; mismo modelo de dominio. La migración a proyecciones separadas se activará solo ante evidencia: latencias de lectura degradadas o consultas analíticas que distorsionen el modelo transaccional.

### Justificación

El análisis inicial mostró un sistema dominado por escritura (8:1). Construir proyecciones de lectura para ese perfil habría sido resolver un problema inexistente pagando costos reales de consistencia. El patrón queda documentado en [guía pública](../patterns/cqrs-buses.md) para escalarlo cuando toque.

## Consecuencias

* **Positivas**: intención explícita por contrato; handlers unitarios y testeables; ruta de migración clara a CQRS completo.
* **Negativas**: lecturas ricas requieren atravesar agregados (mitigable con queries específicas por puerto); dos vocabularios (command/query) que aprender.
* **Neutras**: el bus ya trata commands y queries con decoradores distintos si algún día sus necesidades divergen.

## Validación

Monitoreo de latencias de lectura vía métricas del QueryBus ([ADR-0013](adr-0013-observabilidad-self-hosted.md)). Umbral de revisión: p95 > 200ms sostenido en queries frecuentes → abrir ADR de proyecciones.

## Referencias

* [Patrón público: CQRS y buses](../patterns/cqrs-buses.md)
* [ADR-0020](adr-0020-buses-in-process-command-query-event.md) · [ADR-0021](adr-0021-contratos-records-application.md)
