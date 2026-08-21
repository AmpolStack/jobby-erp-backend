# ADR-0012: Redis como caché con factory propia en `shared`

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

Los servicios necesitan caché para datos de alta lectura y baja escritura (catálogos de referencia, datos de sesión futuros) y para patrones de vida corta (códigos efímeros [ADR-0035](adr-0035-codigos-efimeros-redis.md)). Había que elegir la tecnología y decidir cómo se integra al mecanismo de autoconfiguración de `shared` ([ADR-0005](adr-0005-shared-como-starter-autoconfiguracion.md)).

## Impulsores de la decisión

* Latencia sub-milisegundo para lecturas calientes.
* TTL nativo para datos con expiración semántica (códigos de verificación).
* Control explícito del comportamiento de memoria (eviction policy).
* Integración coherente con el patrón starter: activación condicional y overridable.

## Opciones consideradas

### Opción A: Caché en memoria (Caffeine) por servicio

* Bien, porque cero infraestructura adicional.
* Malo, porque no compartible entre réplicas (inconsistencia de caché por instancia), sin TTL distribuido ni límite global de memoria.

### Opción B: Spring Data Redis estándar (autoconfiguración de Spring Boot)

* Bien, porque cero código propio.
* Malo, porque la autoconfiguración por defecto no encaja con el patrón `@ConditionalOnMissingBean` de `shared`: acoplaría a los servicios a beans difíciles de sobrescribir y mezclaría configuración propia con la del framework.

### Opción C: Redis standalone con factory propia en `shared`

`CacheAutoConfiguration` crea su propia `LettuceConnectionFactory` ligada a propiedades `spring.data.redis.*` vía `@ConfigurationProperties`, y un `RedisTemplate<String, Object>` con clave String + valor Jackson JSON. Todo condicional (`@ConditionalOnClass`) y overridable (`@ConditionalOnMissingBean`).

* Bien, porque control total de serialización y conexión; override limpio; comportamiento de memoria definido en despliegue (`--maxmemory 256mb --maxmemory-policy allkeys-lru`, contraseña obligatoria).
* Malo, porque duplica parcialmente lo que Spring Boot ya autoconfigura (costo aceptado por consistencia del patrón starter).

## Decisión

Opción elegida: **C**. Redis 7+ en Docker Compose con contraseña obligatoria, límite de memoria y política LRU. El puerto `CacheService` vive en dominio; la implementación Redis es un adaptador más ([ADR-0003](adr-0003-ddd-hexagonal-dominio-puro.md)).

Instrumentación: observaciones `redis.register/get/remove` con tags de baja cardinalidad ([ADR-0013](adr-0013-observabilidad-self-hosted.md)); métricas de negocio `cache.hit`/`cache.miss` medidas en un wrapper decorator — el adapter mide si Redis respondió, el wrapper mide si el dato existía (semántica distinta).

### Justificación

Redis es el estándar para este perfil de uso. La factory propia cuesta poco y mantiene intacta la promesa del starter: cada servicio decide si usa caché y puede reemplazarla sin tocar `shared`.

## Consecuencias

* **Positivas**: caché compartible entre réplicas; TTLs semánticos; override total; métricas diferenciadas hit/miss.
* **Negativas**: componente de infraestructura más en el entorno local; riesgo clásico de dependencia de caché (mitigado: la app debe funcionar sin Redis — el adapter falla con `Result`, nunca con excepción).
* **Neutras**: serialización Jackson de valores documentada en la guía interna.

## Validación

Observaciones `redis.*` visibles en Grafana; healthcheck de Redis en compose; tests unitarios del adapter con mocks.

## Referencias

* [ADR-0035](adr-0035-codigos-efimeros-redis.md) · [ADR-0013](adr-0013-observabilidad-self-hosted.md)
