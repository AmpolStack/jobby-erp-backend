# ADR-0005: Módulo `shared` como starter de autoconfiguración

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

Los microservicios de Jobby comparten necesidades transversales: Result Pattern, ValidationChain, buses in-process, transacciones explícitas, cifrado, caché Redis, publicación Kafka, IDs distribuidos, observabilidad. Duplicar esa infraestructura en cada servicio viola DRY; compartirla ingenuamente (una librería que obliga a todas las dependencias) obliga a cada servicio a traer e instalar cosas que no usa.

Había que decidir cómo se distribuye y activa el código transversal.

## Impulsores de la decisión

* Consistencia garantizada: todos los servicios usan la misma implementación de Result, validación, seguridad.
* Activación selectiva: un servicio sin Redis no debe cargar nada de Redis.
* Overridabilidad: un servicio debe poder reemplazar cualquier bean compartido sin tocar `shared`.
* Convención estándar del ecosistema Spring, no invento propio.

## Opciones consideradas

### Opción A: Copiar el código transversal en cada servicio

* Bien, porque independencia absoluta por servicio.
* Malo, porque divergencia inmediata: cinco copias de ValidationChain evolucionando distinto; fixes de seguridad aplicados N veces.

### Opción B: Librería única que todo servicio importa completa

* Bien, porque una sola fuente de verdad.
* Malo, porque classpath inflado con dependencias no usadas (Kafka en un servicio sin mensajería) y beans que fallan al arrancar si falta la infraestructura.

### Opción C: Módulo `shared` estilo Spring Boot Starter

Cada pieza transversal se expone mediante una autoconfiguración condicional registrada en `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, con `@ConditionalOnClass` a nivel de clase y `@ConditionalOnMissingBean` en cada `@Bean`.

* Bien, porque cada capability se activa solo si su clase está en classpath; el servicio puede sobrescribir cualquier bean definiendo el suyo; es exactamente el mecanismo de los starters oficiales post-2.7.
* Malo, porque exige disciplina en el diseño de las autoconfiguraciones y metadata de propiedades para IDE.

## Decisión

Opción elegida: **C — `shared` como starter**. Hoy registra 15 autoconfiguraciones (Cache, Encryption, Hashing, Mac, MessagePublisher, SafeValidator, IdGenerator, Transaction, SecurityOrchestrator, APIMapper, FileStorageService, EmailService, OptionalOperation, EventRegistry, TransformationRegistry).

Patrón fijo:

```java
@Configuration
@ConditionalOnClass(RedisTemplate.class)          // solo si la tech existe en classpath
public class CacheAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(CacheService.class) // overridable por el servicio
    public CacheService cacheService(...) { ... }
}
```

### Justificación

Es el patrón nativo del ecosistema: cero magia propia, comportamiento documentado por Spring Boot, y resuelve simultáneamente consistencia (una fuente), selectividad (condicionales) y libertad (override). Alineado con el principio *explicitness over magic*: la activación es visible y predecible.

## Consecuencias

* **Positivas**: consistencia transversal; servicios ligeros según lo que usan; override limpio por servicio; metadata de propiedades con autocomplete en IDE.
* **Negativas**: cambios en `shared` afectan a todos los consumidores (mitigado por monorepo [ADR-0004](adr-0004-monorepo-maven-multi-modulo.md)); autoconfiguraciones mal diseñadas serían difíciles de depurar.
* **Neutras**: `shared` también publica test-jar con utilidades de testing ([ADR-0036](adr-0036-stack-testing-resultassertions-test-jar.md)).

## Validación

Toda nueva pieza transversal debe entrar con su autoconfiguración condicional y su entrada en `AutoConfiguration.imports`; review verifica el par `@ConditionalOnClass`/`@ConditionalOnMissingBean`.

## Referencias

* [Guía de arquitectura — Autoconfiguración](../../internal/guides/architecture-rules.md)
* Spring Boot docs — Auto-configuration Customization
