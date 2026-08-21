# ADR-0018: Taxonomía de errores de tres niveles con sanitización

* Fecha: 2026-08-20 (retro-documentada)
* Estado: aceptada

## Contexto y problema

Con Result Pattern ([ADR-0016](adr-0016-result-pattern-unico.md)), todo fallo viaja como `Error` tipado. Pero no todos los errores son iguales: un "email ya registrado" debe mostrarse tal cual al usuario; un "fallo de deserialización Avro" contiene detalles internos (clases, rutas, versiones) que jamás deben salir del sistema — pero sí deben loguearse con precisión para soporte.

Había que decidir cómo se clasifican, propagan y exponen los errores.

## Impulsores de la decisión

* Seguridad de información: detalles internos nunca visibles al cliente.
* Trazabilidad: el mismo código de error debe servir para buscar en logs.
* Mapeo determinista error → HTTP status.
* Propagación entre capas sin perder contexto útil.

## Opciones consideradas

### Opción A: Strings libres como código de error

* Bien, porque flexibilidad total.
* Malo, porque sin catálogo no hay política posible: cada desarrollador inventa códigos y la sanitización es manual (y olvidadiza).

### Opción B: Jerarquía de clases de excepción por severidad

* Bien, porque tipado fuerte.
* Malo, porque choca con Result Pattern; la clasificación por herencia es rígida y difícil de serializar hacia HTTP.

### Opción C: Enum `ErrorType` con taxonomía codificada por prefijo

Tres categorías en un solo enum:

| Prefijo | Audiencia | Ejemplos | Tratamiento |
|---|---|---|---|
| *(sin prefijo)* | Usuario final | `NOT_FOUND`, `VALIDATION_ERROR`, `DUPLICATE_RESOURCE` | Mensaje mostrable tal cual |
| `ITN_*` | Interna (validación) | `ITN_ENCRYPTION_FAILURE` | Sanitizado antes de exponer |
| `ITS_*` | Sistema/configuración | `ITS_SERIALIZATION_ERROR`, `ITS_EXTERNAL_SERVICE_FAILURE` | Sanitizado + log nivel error |

Métodos del enum: `toSanitizedError()` reemplaza el detalle real por mensaje genérico y lo loguea con nivel distinto (error vs warn); mapeo ErrorType→HttpStatus vía `EnumMap` estático; registro de tipos expuestos en `ErrorTypeHttpCollection`.

Regla de propagación (`propagateFailure`): los códigos `ITN_*` conservan el nombre de campo original al propagarse; el resto lo reemplaza — evita filtrar nombres de campos internos.

* Bien, porque la categoría viaja *dentro* del código: imposible confundir audiencias; sanitización centralizada y automática; búsqueda en logs trivial por prefijo.
* Malo, porque el enum crece con el dominio (aceptado: catálogo curado); convención de prefijos que documentar.

## Decisión

Opción elegida: **C**. Los logs de adapters llevan prefijo `[ITS_*]`/`[VALIDATION_ERROR]` correlacionado con el enum, cerrando el círculo error→log→soporte.

### Justificación

Codificar la audiencia en el identificador convierte una política de seguridad en una propiedad mecánica del tipo. La sanitización deja de depender de que alguien "recuerde" limpiar el mensaje: el tipo equivocado ni siquiera puede llegar al cliente sin pasar por `toSanitizedError()`.

## Consecuencias

* **Positivas**: fuga de información estructuralmente impedida; soporte puede buscar `[ITS_...]` directamente; mapeo HTTP determinista.
* **Negativas**: disciplina para elegir la categoría correcta al crear códigos nuevos (review + skill planificado).
* **Neutras**: integración con Problem Details en [ADR-0031](adr-0031-problem-details-rfc7807.md).

## Validación

Tests sobre sanitización y propagación; revisión periódica del catálogo `ErrorTypeHttpCollection`; skill `problem-details-errors` planificado ([ADR-0038](adr-0038-skills-opencode-guardianes.md)).

## Referencias

* [Guía interna de validación](../../internal/guides/validation-guide.md) · [ADR-0030](adr-0030-sanitizacion-errores-support-id.md)
