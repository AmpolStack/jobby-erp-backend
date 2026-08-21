# ADR-0031: Problem Details (RFC 7807) para API externa

* Fecha: 2026-08-20 (retro-documentada)
* Estado: aceptada

## Contexto y problema

La API REST de Jobby ERP será consumida por tres audiencias con necesidades distintas: la UI propia, integradores externos (terceros conectando sus ERPs/POS) y los servicios internos entre sí. Los errores HTTP necesitaban un formato consistente, estándar y auto-explicable — sin inventar un dialecto propio que cada integrador debiera aprender.

## Impulsores de la decisión

* Estándar abierto para errores HTTP: interoperabilidad inmediata con cualquier cliente.
* Diferenciación de audiencias: externos reciben formato rico; comunicación interna optimizada.
* Integración con sanitización ([ADR-0030](adr-0030-sanitizacion-errores-support-id.md)) y taxonomía ([ADR-0018](adr-0018-taxonomia-errores-tres-niveles.md)).
* Prohibición explícita de devolver excepciones crudas desde controllers.

## Opciones consideradas

### Opción A: Formato propio ad-hoc para todo

* Bien, porque control total.
* Malo, porque cada integrador aprende un dialecto no estándar; herramientas genéricas (gateways, SDKs) no lo entienden.

### Opción B: Solo RFC 7807 para absolutamente todo

* Bien, porque uniformidad total.
* Malo, porque el overhead del formato completo es innecesario en llamadas servicio-a-servicio de altísima frecuencia donde ambos extremos comparten tipos.

### Opción C: RFC 7807 para APIs externas + formato simple interno

- **Externa**: Problem Details (`type`, `title`, `status`, `detail`, más extensiones como `supportId`) vía `HttpResponseProcessor` + `ProblemDetailsResultMapper`; en éxito, el DTO plano sin envoltura.
- **Interna**: formato simplificado entre servicios (menor costo de serialización), documentado como contrato interno.
- Registro de ErrorTypes expuestos en `ErrorTypeHttpCollection`; mapeo determinista a HttpStatus.

* Bien, porque estándar donde importa (frontera pública) y eficiencia donde el estándar sobra (interno); existe también `ResultHttpMapper` (devuelve Result crudo) pero no está cableada por defecto.
* Malo, porque dos formatos que documentar (mitigado: la frontera es clara — fuera del clúster vs dentro).

## Decisión

Opción elegida: **C**. Reglas:

1. Controllers jamás retornan excepciones ni Results crudos; siempre procesan vía `HttpResponseProcessor`: `validator.validate(request).flatMap(v -> commandBus.dispatch(mapper.toCommand(...)))` → `response.map(processor)`.
2. Éxito = DTO plano (sin envoltura Result visible al cliente).
3. Fallo = Problem Details con detalle sanitizado + supportId.
4. Comunicación inter-servicio usa el formato simple interno.

### Justificación

RFC 7807 es el estándar correcto en la frontera donde viven terceros; el formato simple interno es la optimización correcta donde ambos extremos son nuestros. La decisión respeta ambas realidades sin dogmatismo.

## Consecuencias

* **Positivas**: integradores con experiencia estándar; errores auto-documentados; soporte correlacionable vía supportId.
* **Negativas**: mantener dos mappers de respuesta (costo mínimo, ya implementado).
* **Neutras**: OpenAPI documenta los formatos por endpoint ([README](../../../README.md) — API specification).

## Validación

Tests de controllers verifican forma Problem Details ante fallos y DTO plano ante éxitos; skill `problem-details-errors` planificado ([ADR-0038](adr-0038-skills-opencode-guardianes.md)).

## Referencias

* [RFC 7807 — Problem Details for HTTP APIs](https://datatracker.ietf.org/doc/html/rfc7807)
* [ADR-0016](adr-0016-result-pattern-unico.md) · [ADR-0030](adr-0030-sanitizacion-errores-support-id.md)
