# ADR-0014: Snowflake para IDs distribuidos

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

Los agregados necesitan identificadores únicos generados por la aplicación (no por la BD), porque la generación ocurre en el dominio/adaptadores antes de persistir y porque con Database per Service ([ADR-0002](adr-0002-microservicios-con-database-per-service.md)) no hay secuencias centrales. Además, el sistema está diseñado para múltiples instancias por servicio desde el inicio.

## Impulsores de la decisión

* Unicidad garantizada entre instancias y servicios sin coordinación central.
* IDs ordenables temporalmente (útiles para orden natural y debugging).
* 64 bits compactos, aptos para índices y para viajar en eventos.
* Configurabilidad por entorno (worker/datacenter) para escalar horizontalmente.

## Opciones consideradas

### Opción A: UUID v4 aleatorio

* Bien, porque generación trivial sin coordinación.
* Malo, porque 128 bits desordenados degradan índices (inserciones dispersas) y son ilegibles en logs/soporte.

### Opción B: Secuencia de base de datos

* Bien, porque orden estricto y simplicidad.
* Malo, porque acopla generación de ID a la BD (contradice que el dominio lo genere), cuello de botella potencial, y no funciona cross-servicio.

### Opción C: Snowflake (implementación Hutool)

ID de 64 bits: timestamp + datacenter-id + worker-id + secuencia. Expuesto como puerto `IdGenerator` que retorna `Result<Long, Error>`, implementado por `SnowflakeIdGenerator` con autoconfiguración condicional ([ADR-0005](adr-0005-shared-como-starter-autoconfiguracion.md)).

* Bien, porque unicidad distribuida sin coordinación; orden temporal aproximado; configuración `app.id.datacenter-id/worker-id` por instancia.
* Malo, porque requiere asignar worker/datacenter únicos por instancia (operación manual hoy); dependencia del reloj del sistema.

## Decisión

Opción elegida: **C — Snowflake vía Hutool**, envuelto en puerto propio.

### Justificación

Es el equilibrio estándar de la industria para IDs distribuidos en JVM: compacto, ordenable, sin infraestructura adicional. Envolverlo en un puerto propio mantiene el dominio libre de Hutool ([ADR-0003](adr-0003-ddd-hexagonal-dominio-puro.md)) y permite cambiar la implementación sin tocar consumidores.

## Consecuencias

* **Positivas**: escalado horizontal listo; IDs legibles en soporte (contienen tiempo); retorno `Result` coherente con el estilo transversal.
* **Negativas**: gestión manual de worker-ids al escalar (riesgo de duplicados si se configura mal — documentado en guía); sensibilidad a saltos de reloj.
* **Neutras**: la deuda conocida "sacar la generación de ID del caso de uso" ([backlog](../../internal/backlog.md)) es ortogonal a esta elección.

## Validación

Tests unitarios de unicidad/secuencia; configuración verificada en startup; observación futura de colisiones imposible por diseño si los ids de instancia son correctos.

## Referencias

* [ADR-0003](adr-0003-ddd-hexagonal-dominio-puro.md) · [Backlog técnico](../../internal/backlog.md)
