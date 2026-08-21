# ADR-0039: Principio "explicitness over magic"

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

Los frameworks modernos ofrecen automatismos potentes: escaneo classpath de handlers, proxies AOP para transacciones/eventos, reflexión para wiring, convención sobre configuración extrema. Cada uno ahorra código hoy y cobra legibilidad mañana: cuando algo falla, el desarrollador depura *el framework* en vez de *su lógica*.

Este principio no surgió de un evento único sino como patrón recurrente: una y otra vez, al evaluar alternativas, la opción "mágica" perdía frente a la explícita. Este ADR formaliza ese criterio para que futuras decisiones no re-debatan lo ya decidido.

## Impulsores de la decisión

* Debuggabilidad: los fallos deben rastrearse leyendo código propio.
* Objetivo educativo: lo explícito enseña; lo mágico oculta.
* Predictibilidad: comportamiento visible = estimaciones confiables.
* Costo cognitivo: cada automatismo es una regla implícita que alguien debe descubrir.

## Opciones consideradas

### Opción A: Maximizar automatismos del framework

* Bien, porque menos código propio.
* Malo, porque wiring invisible, fallos runtime crípticos, dependencia profunda del comportamiento interno del framework.

### Opción B: Explicitness como criterio absoluto sin excepciones

* Bien, porque pureza conceptual.
* Malo, porque dogmatismo: algunos automatismos maduros (inyección de dependencias, autoconfiguración condicional estándar) son más confiables que su versión manual.

### Opción C: Explicitness como criterio de desempate con excepciones razonadas

Preferir siempre la opción cuyo comportamiento pueda trazarse leyendo código propio — salvo cuando el automatismo sea estándar maduro del ecosistema, esté documentado por su vendor, y su falla sea diagnosticable.

* Bien, porque elimina clases enteras de bugs (registro olvidado, proxy no aplicado) a cambio de líneas de registro visibles; coherente con todos los ADRs previos.
* Malo, porque exige justificar cada uso de magia aceptada (costo asumido: es exactamente el punto).

## Decisión

Opción elegida: **C**. Aplicaciones concretas ya decididas:

| Decisión | Magia rechazada | Explicitud elegida |
|---|---|---|
| [ADR-0020](adr-0020-buses-in-process-command-query-event.md) | Escaneo classpath de handlers | Registro fluido manual |
| [ADR-0022](adr-0022-eventos-dominio-pull-domain-events.md) | AOP `@AfterReturning` para ordeñar eventos | Ordeñe manual + tests + skill |
| [ADR-0022](adr-0022-eventos-dominio-pull-domain-events.md) | Collector request-scoped automático | Pull explícito post-transacción |
| [ADR-0019](adr-0019-fabrica-doble-of-reconstruct.md) | Constructores accesibles + validación opcional | Fábrica única puerta obligatoria |
| [ADR-0005](adr-0005-shared-como-starter-autoconfiguracion.md) | Autoconfiguración opaca propia | Condicionales estándar Spring documentadas |

Magia **aceptada** (estándar maduro): inyección de dependencias Spring, autoconfiguración condicional oficial, proxy transaccional dentro del TransactionOrchestrator.

### Justificación

El criterio con excepciones razonadas evita tanto el caos mágico como el masoquismo artesanal. La tabla de aplicaciones hace el principio auditable: cada nueva tentación mágica se compara contra ella.

## Consecuencias

* **Positivas**: sistema depurable por lectura; onboarding honesto (lo que ves es lo que hay); decisiones futuras con criterio pre-acordado.
* **Negativas**: más líneas de registro/wiring explícito (compradas deliberadamente).
* **Neutras**: formalizado como [principio 1](../../project/principles.md) del proyecto.

## Validación

Review cuestiona todo automatismo nuevo contra este ADR; las excepciones aceptadas viven listadas aquí y se actualizan solo vía nuevo ADR.

## Referencias

* [Principios del proyecto](../../project/principles.md)
