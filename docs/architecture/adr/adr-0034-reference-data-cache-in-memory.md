# ADR-0034: Reference data como caché in-memory read model

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

El sistema depende de catálogos de referencia esencialmente estáticos: tipos de identificación (CC, NIT, PPT...), tipos de contacto, departamentos y municipios de Colombia (datos DANE). El diseño original los modelaba como agregados con repositorio — lo que el análisis (`analisis-domain-application`, hoy absorbido aquí) identificó como **falsos agregados**: no tienen ciclo de vida propio, ni invariantes que evolucionar, ni acciones de negocio; solo se consultan.

## Impulsores de la decisión

* Los catálogos cambian con frecuencia anual (o menor), no diaria.
* Se consultan en cada operación de escritura (validar tipo de identificación).
* Evitar N queries por request para datos que nunca cambian en runtime.
* Modelado honesto: si no es un agregado, no debe tratarlo como tal.

## Opciones consideradas

### Opción A: Agregados con repositorio (estado original)

* Bien, porque uniformidad conceptual ("todo pasa por puertos").
* Malo, porque ceremonia sin valor: puertos, adapters y transacciones para datos estáticos; query a Mongo por cada validación de catálogo.

### Opción B: Hardcodear los catálogos en código

* Bien, porque cero latencia.
* Malo, porque actualizar catálogos (nuevo municipio DANE, nuevo tipo de documento DIAN) exige redeploy; imposible de gestionar por operadores.

### Opción C: Caché in-memory read model cargada al startup

`ReferenceDataCache` como adapter de infraestructura: maps `volatile` inmutables cargados una vez vía `@PostConstruct` desde MongoDB; `Department` se deriva de los municipalities (sin colección propia consultable); consulta fallida por id inexistente → `VALIDATION_ERROR`.

* Bien, porque cero latencia en hot path; persistencia real (Mongo) para gestión futura; modelado honesto (read model, no agregado); actualización = reinicio o refresh manual.
* Malo, porque cambios de catálogo requieren refresh/reinicio (aceptable: frecuencia anual); memoria proporcional al catálogo (trivial: miles de registros).

## Decisión

Opción elegida: **C**. Reglas:

1. Los catálogos **no** son agregados: sin puertos de repositorio propios, sin eventos, sin transacciones.
2. La caché es un read model de infraestructura; su invalidación manual está expuesta para operaciones administrativas futuras.
3. Si algún día los catálogos se vuelven dinámicos (gestionados por UI en runtime), se abrirá ADR de invalidación distribuida.

### Justificación

Reclasificar falsos agregados como read models hace que el modelo de dominio diga la verdad: lo que solo se consulta, se modela como consulta. El costo de la decisión es un refresh manual ante cambios anuales — despreciable frente al beneficio en el hot path.

## Consecuencias

* **Positivas**: validaciones de catálogo sin I/O; dominio sin ruido de "agregados" falsos; datos reales DIAN/DANE versionados en BD.
* **Negativas**: ventana de obsolescencia hasta refresh (documentada); doble fuente (BD + caché) que mantener coherentes.
* **Neutras**: complementa Redis ([ADR-0012](adr-0012-redis-cache-factory-propia.md)): esto es caché local por instancia para datos ultra-estáticos; Redis queda para datos compartidos/efímeros.

## Validación

Tests del cache adapter verifican carga, derivación de departments y fallo explícito por id inexistente.

## Referencias

* [ADR-0027](adr-0027-validacion-distribuida-por-capas.md) — dónde vive la validación contra catálogos
