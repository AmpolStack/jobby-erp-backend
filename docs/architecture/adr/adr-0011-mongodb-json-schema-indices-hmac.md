# ADR-0011: MongoDB con JSON Schema strict e índices sobre HMAC

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

Los servicios necesitan persistencia operacional para agregados con estructura rica (agregados con colecciones anidadas, VOs compuestos) y evolución frecuente en fase temprana. Además, el proyecto cifra datos personales a nivel de campo ([ADR-0028](adr-0028-cifrado-campo-indice-hmac-searchable.md)), lo que plantea una pregunta de persistencia: ¿cómo se garantiza unicidad y búsqueda sobre datos cifrados?

## Impulsores de la decisión

* Agregados DDD con documentos anidados mapean naturalmente a documentos.
* Esquema evolutivo sin migraciones pesadas durante la fase de descubrimiento del producto.
* Necesidad de constraints UNIQUE sobre campos cifrados (email, teléfono, identificación).
* Validación estructural incluso cuando la aplicación falla.

## Opciones consideradas

### Opción A: PostgreSQL relacional

* Bien, porque integridad referencial, transacciones ricas, SQL analítico.
* Malo, porque el impedance mismatch obliga a aplanar agregados o abusar de JSONB (perdiendo las garantías relacionales igualmente); migraciones de esquema más costosas en fase temprana.

### Opción B: MongoDB sin validación de esquema

* Bien, porque flexibilidad total.
* Malo, porque "sin esquema" en la práctica significa "esquema implícito en el código": corrupción silenciosa ante bugs de mapeo.

### Opción C: MongoDB 8 con JSON Schema strict + índices únicos sobre índices HMAC

Colecciones con `validator` JSON Schema (`validationLevel: strict`, `additionalProperties: false`) definidas en script de inicialización; índices únicos creados **sobre los campos `.index` (HMAC)**, nunca sobre los datos cifrados.

* Bien, porque documento = agregado sin mismatch; la BD rechaza estructuras inválidas aunque la aplicación tenga un bug; la unicidad de email/teléfono/identificación se garantiza vía su HMAC determinista ([ADR-0028](adr-0028-cifrado-campo-indice-hmac-searchable.md)).
* Malo, porque los validators deben mantenerse sincronizados con las entidades (script versionado); transacciones multi-documento requieren replica set.

## Decisión

Opción elegida: **C**. Convenciones aplicadas:

- Colecciones en snake_case con validator strict por colección (`users`, `owners`, `employees`, catálogos de referencia).
- Índices únicos: `{ "email.index": 1 }`, `{ "identification_number.index": 1 }`, `{ "phone.index": 1 }`.
- Script de inicialización versionado en `user-service/docker/sources/mongo/mongo-init-script.js`.
- Transacciones explícitas gestionadas por `TransactionOrchestrator` ([ADR-0025](adr-0025-transacciones-explicitas-transaction-orchestrator.md)), no dispersas por el código.

### Justificación

El modelo de documentos elimina el impedance mismatch que un ORM relacional introduciría entre agregado y tabla. El JSON Schema strict recupera lo único valioso que la flexibilidad sacrificaría: garantías estructurales. Y la combinación con índices HMAC resuelve elegantemente la paradoja "dato cifrado pero con UNIQUE constraint".

## Consecuencias

* **Positivas**: agregados persistidos como documentos naturales; doble red de seguridad (validación en VOs + validator en BD); unicidad real sobre PII cifrada.
* **Negativas**: joins inexistentes (irrelevante bajo Database per Service); validators duplican parcialmente el conocimiento del esquema.
* **Neutras**: otros servicios podrán elegir otro motor si su dominio lo justifica — Database per Service lo permite ([ADR-0002](adr-0002-microservicios-con-database-per-service.md)).

## Validación

El script de init corre en cada levantamiento del entorno local; tests de integración planificados con Testcontainers validarán validators e índices ([backlog](../../internal/backlog.md)).

## Referencias

* [ADR-0028](adr-0028-cifrado-campo-indice-hmac-searchable.md) — cifrado y HMAC · [ADR-0033](adr-0033-colecciones-separadas-users-owners-employees.md) — modelado de colecciones
