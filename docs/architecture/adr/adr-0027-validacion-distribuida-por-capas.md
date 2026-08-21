# ADR-0027: Validación distribuida por capas sin duplicación

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

La misma regla puede validarse en varios puntos: ¿el formato del email se valida en el controller, en el VO, en el caso de uso o en la BD? Validar en todas partes es redundante y caro; validar en ninguna es un bug. El análisis (`validacion-por-capas`, hoy absorbido aquí) detectó además un caso sutil de "validar y descartar": un mapper que construía VOs validados pero terminaba pasando los strings crudos a las factories.

Había que fijar la política: **qué valida cada capa y solo esa**.

## Impulsores de la decisión

* Una regla, un dueño: si el mismo chequeo aparece en dos capas, uno sobra.
* Errores lo más cerca posible del origen del dato.
* Fail-fast para configuración; validación rica para dominio.
* Evitar el anti-patrón "validar y descartar" (construir VO validado y usar el raw).

## Opciones consideradas

### Opción A: Validación centralizada en una única capa (ej. todo en controllers)

* Bien, porque un solo lugar que revisar.
* Malo, porque reglas de dominio (unicidad, consistencia interna) no pueden vivir fuera del dominio; controllers sobrecargados; servicios internos sin protección.

### Opción B: Defensa en profundidad — validar en todas las capas

* Bien, porque máxima seguridad percibida.
* Malo, porque duplicación masiva; mensajes contradictorios posibles; costo computacional y de mantenimiento sin beneficio real.

### Opción C: Distribución por responsabilidad con dueño único

| Capa | Valida | Ejemplo |
|---|---|---|
| Config (`@ConfigurationProperties`) | Propiedades propias | fail-fast en startup con Jakarta Validation |
| Controller | Estructura HTTP del request | `SafeResultValidator` rechaza peticiones malformadas antes del caso de uso |
| HttpMapper / CommandMapper | Solo reorganiza campos; dispara validación de VOs | `Email.of(raw)` — y pasa **el VO**, nunca el raw |
| Value Object | Reglas intrínsecas del dato | formato email, rangos de longitud |
| Aggregate | Consistencia interna | invariantes entre entidades hijas |
| Caso de uso | Reglas con dependencias externas | unicidad (`existByEmail`), existencia previa |
| Servicio/Adapter | Solo sus entradas, nunca su configuración | config ya fue validada en startup |

* Bien, porque cada chequeo tiene exactamente un hogar semántico; errores contextualizados por capa; sin duplicación.
* Malo, porque exige entender la tabla para ubicar validaciones nuevas (documentado en guía + skill).

## Decisión

Opción elegida: **C**, con las reglas operativas:

1. `CommandMapper` es el punto de entrada que convierte crudos → VOs vía `of()`; debe propagar **VOs validados**, jamás strings crudos hacia `reconstruct()`.
2. Los HttpMappers solo reorganizan campos; no validan.
3. Jakarta Validation declarativa permitida únicamente en `@ConfigurationProperties` (y opcionalmente requests REST como filtro estructural temprano).
4. La unicidad y toda regla que requiera repositorio vive exclusivamente en el caso de uso.

### Justificación

Cada capa valida lo que *solo ella puede saber*: la estructura la conoce el controller, la intrínseca el VO, la relacional el caso de uso. La distribución correcta elimina la duplicación sin sacrificar cobertura — y corrige de raíz el bug de "validar y descartar".

## Consecuencias

* **Positivas**: cero chequeos duplicados; mensajes de error con contexto correcto; startup fail-fast para configuración.
* **Negativas**: curva de aprendizaje sobre dónde va cada validación nueva (mitigado: guía + skill `aggregate-invariants`).
* **Neutras**: integración natural con ValidationChain ([ADR-0017](adr-0017-validationchain-perezosa.md)) y taxonomía de errores ([ADR-0018](adr-0018-taxonomia-errores-tres-niveles.md)).

## Validación

Review verifica ubicación de validaciones nuevas; tests por capa confirman que cada una rechaza lo suyo; skill planificado automatiza la verificación.

## Referencias

* [Guía interna de validación](../../internal/guides/validation-guide.md)
* [ADR-0017](adr-0017-validationchain-perezosa.md) · [ADR-0019](adr-0019-fabrica-doble-of-reconstruct.md)
