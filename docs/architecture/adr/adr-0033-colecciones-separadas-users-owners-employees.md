# ADR-0033: Colecciones separadas users/owners/employees — NO embeber User

* Fecha: 2026-08-20 (retro-documentada)
* Estado: aceptada

## Contexto y problema

En el modelo de negocio, un Owner (propietario del negocio) y un Employee son personas con datos de identidad compartidos (nombre, email, teléfono, identificación). Surgió la tentación de embeber el documento `User` dentro de `Owner` para "evitar duplicación" y aprovechar el modelo documental de MongoDB. La decisión (`decision-users-owners-embedded`, hoy absorbida aquí) analizó el trade-off en profundidad.

## Impulsores de la decisión

* User, Owner y Employee son **agregados distintos** con ciclos de vida e invariantes propios.
* El futuro `auth-service` consumirá User sin conocer Owner ([ADR-0002](adr-0002-microservicios-con-database-per-service.md)).
* Una misma persona puede ser Owner y Employee simultáneamente.
* Las actualizaciones de identidad (cambiar email) no deben propagarse en cascada entre documentos.

## Opciones consideradas

### Opción A: Embeber User dentro de Owner

* Bien, porque lectura atómica del agregado compuesto; cero joins.
* Malo, porque rompe los bounded contexts: auth tendría que leer Owners para autenticar; una persona Owner+Employee duplicaría su identidad en dos documentos (actualizar email = actualizar N documentos o bugs silenciosos); el tamaño del documento crece innecesariamente.

### Opción B: Colecciones separadas con referencia por ID

`owners` referencia `userId`; cada agregado vive independiente.

* Bien, porque contextos limpios: auth consume Users directamente; una persona es un solo User referenciado por N roles; actualizar identidad = un solo `updateOne`.
* Malo, porque lecturas compuestas requieren dos queries (aceptable: la composición Owner→User es estable y cacheable).

### Opción C: Un mega-agregado Persona con roles

* Bien, porque unicidad estructural garantizada.
* Malo, porque agrega conceptos normativos ajenos (roles) al núcleo de identidad; aggregate gigante con invariantes difusas.

## Decisión

Opción elegida: **B — colecciones separadas** (`users`, `owners`, `employees`) con referencia por ID. Condiciones documentadas para reconsiderar el embedding:

> Si en producción las lecturas compuestas Owner→User demostraran ser cuello de botella Y los contextos nunca se separen físicamente, el embedding puede reabrirse como nuevo ADR.

Reglas complementarias derivadas:

1. `Owner.create()` recibe el agregado `User` completo (no un `long userId`) — garantiza integridad referencial en creación y da acceso a datos necesarios para eventos.
2. La duplicación controlada de VOs entre contextos es preferible a acoplarlos ([principio 7](../../project/principles.md)).

### Justificación

El embedding optimiza la lectura a costa del acoplamiento — exactamente el intercambio equivocado para un sistema que planea extraer auth-service. La referencia por ID mantiene los bounded contexts honestos y las actualizaciones de identidad triviales.

## Consecuencias

* **Positivas**: contextos preparados para extracción de servicios; identidad única actualizable en un punto; agregados cohesivos.
* **Negativas**: lecturas compuestas multi-query (mitigable con proyecciones si hiciera falta — [ADR-0006](adr-0006-cqrs-parcial.md)).
* **Neutras**: índices y validators por colección independientes ([ADR-0011](adr-0011-mongodb-json-schema-indices-hmac.md)).

## Validación

La extracción futura de auth-service será la prueba definitiva: debe poder hacerse sin migración de datos de identidad.

## Referencias

* [ADR-0002](adr-0002-microservicios-con-database-per-service.md) · [ADR-0011](adr-0011-mongodb-json-schema-indices-hmac.md)
* [ADR-0022](adr-0022-eventos-dominio-pull-domain-events.md) — eventos del agregado Owner
