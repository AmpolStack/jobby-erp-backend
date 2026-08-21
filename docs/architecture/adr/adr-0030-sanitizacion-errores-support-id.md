# ADR-0030: Sanitización de errores internos + supportId trazable

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

Cuando algo falla en infraestructura (serialización, servicio externo, configuración), el detalle técnico del error — clases, rutas, versiones, mensajes de librerías — es oro para el desarrollador y munición para un atacante. La política debe decidir qué ve cada audiencia: el usuario final necesita saber *que* falló y qué hacer; el soporte necesita correlacionar el reporte del usuario con la traza exacta; el log necesita el detalle completo.

## Impulsores de la decisión

* Cero fuga de detalles internos hacia clientes HTTP.
* Un usuario que reporta "falló X" debe poder ser rastreado a la traza exacta sin preguntarle nada más.
* Integración natural con la taxonomía de errores ([ADR-0018](adr-0018-taxonomia-errores-tres-niveles.md)) y Problem Details ([ADR-0031](adr-0031-problem-details-rfc7807.md)).

## Opciones consideradas

### Opción A: Mensajes genéricos para todo ("Internal Server Error")

* Bien, porque máxima seguridad.
* Malo, porque inutiliza errores legítimos de validación; el soporte no puede correlacionar nada.

### Opción B: Propagar mensajes técnicos completos al cliente

* Bien, porque debugging "fácil" desde el cliente.
* Malo, porque fuga directa de información interna (stack traces, nombres de campos internos, tecnología usada).

### Opción C: Sanitización por categoría + supportId = traceId

- `ErrorType.toSanitizedError()` reemplaza el detalle real por mensaje genérico **y lo loguea** con nivel diferenciado (`error` para `ITS_*`, `warn` para `ITN_*`).
- Los códigos `ITN_*` conservan el nombre de campo original al propagarse (`propagateFailure`); el resto lo reemplaza — nunca se filtran nombres de campos internos.
- En fallo, la respuesta Problem Details incluye `supportId` = traceId actual de Micrometer Tracer (`"unavailable"` si no hay span activo).
- El usuario reporta el supportId; soporte lo busca en Zipkin/Loki y obtiene la traza completa con todos los detalles técnicos.

* Bien, porque cada audiencia recibe exactamente su nivel de detalle; correlación usuario→traza en un paso; sanitización automática por tipo, no por memoria.
* Malo, porque requiere instrumentación de tracing activa en todos los puntos de fallo ([ADR-0013](adr-0013-observabilidad-self-hosted.md) la garantiza).

## Decisión

Opción elegida: **C**, implementada vía `HttpResponseProcessor` + `ProblemDetailsResultMapper` + `SupportIdProvider`. Flujo:

```
Fallo interno → ErrorType ITS_* → toSanitizedError() → log detallado [ITS_...]
                                                      → Problem Details {detail genérico, supportId=traceId}
Usuario reporta supportId → soporte busca en Zipkin/Loki → traza completa
```

### Justificación

Convierte la tensión seguridad↔debuggability en una separación por canal: el canal público (HTTP) solo lleva lo seguro más un identificador opaco; los canales privados (logs, trazas) llevan el detalle. El supportId es el puente entre ambos sin exponer nada.

## Consecuencias

* **Positivas**: fuga estructuralmente impedida (el tipo equivocado ni siquiera llega al mapper HTTP); soporte eficiente; UX honesta ante fallos.
* **Negativas**: dependencia de tracing para el valor completo del supportId (degradación elegante a `"unavailable"`).
* **Neutras**: complementa la convención de logs `[ERROR_TYPE]` ([ADR-0013](adr-0013-observabilidad-self-hosted.md)).

## Validación

Tests verifican que respuestas de fallos `ITS_*` contienen detalle genérico + supportId; revisión manual en Zipkin de la correlación; skill `problem-details-errors` planificado ([ADR-0038](adr-0038-skills-opencode-guardianes.md)).

## Referencias

* [ADR-0018](adr-0018-taxonomia-errores-tres-niveles.md) · [ADR-0031](adr-0031-problem-details-rfc7807.md)
