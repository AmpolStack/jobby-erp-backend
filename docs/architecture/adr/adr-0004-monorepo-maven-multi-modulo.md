# ADR-0004: Monorepo Maven multi-módulo mientras el equipo sea pequeño

* Fecha: 2026-08-20 (retro-documentada)
* Estado: aceptada

## Contexto y problema

Con Database per Service ([ADR-0002](adr-0002-microservicios-con-database-per-service.md)), cada servicio es una unidad desplegable independiente. Había que decidir dónde vive el código: un repositorio por servicio o un repositorio único con todos.

El proyecto arranca con un equipo mínimo y servicios que comparten el módulo `shared` ([ADR-0005](adr-0005-shared-como-starter-autoconfiguracion.md)) en evolución constante.

## Impulsores de la decisión

* Equipo pequeño (< 10 personas) sin capacidad de operar N pipelines ni versionar N librerías coordinadamente.
* `shared` cambia a diario junto con los servicios que lo consumen.
* Atomicidad deseable: un cambio transversal (ej. nueva regla en ValidationChain) debe poder aterrizarse en un solo PR.
* Contribuyentes open source: un solo clone = todo el contexto.

## Opciones consideradas

### Opción A: Polyrepo — un repositorio por servicio

* Bien, porque independencia total de releases, permisos y CI por servicio.
* Malo, porque versionar `shared` exige publicar artefactos y coordinar upgrades en cada servicio; los cambios transversales se vuelven proyectos de semanas; descubrir código entre repos es fricción constante para un equipo chico.

### Opción B: Monorepo Maven multi-módulo

Un repositorio, POM raíz con módulos (`shared`, `user-service`, futuros servicios), builds agregados.

* Bien, porque cambios atómicos transversales; CI simple al inicio; refactors cross-servicio triviales; un solo lugar para contribuir.
* Malo, porque el repo crece y el CI puede volverse lento si no se filtra por rutas; requiere disciplina para no crear dependencias circulares entre módulos de servicio.

## Decisión

Opción elegida: **B — Monorepo Maven multi-módulo**, con regla de salida explícita:

> Se migrará a polyrepo cuando existan ciclos de release independientes reales entre servicios o cuando el equipo supere ~10 personas.

Estructura actual:

```
jobby/
├── pom.xml          (packaging pom, BOM Spring Boot + OpenTelemetry)
├── shared/          → starter transversal
└── user-service/    → primer servicio de negocio
```

### Justificación

Para un equipo pequeño construyendo en público, la fricción dominante es la velocidad de iteración, no la gobernanza de releases. El monorepo elimina la fricción de versionamiento mientras los servicios comparten evolución temprana. La condición de salida está escrita *antes* de necesitarla, para que la migración no sea una decisión emocional sino un criterio medible.

## Consecuencias

* **Positivas**: PRs atómicos cross-módulo; onboarding simple; CI inicial trivial; refactors de `shared` instantáneos.
* **Negativas**: CI deberá filtrar por rutas conforme crezca; riesgo de acoplamiento encubierto entre servicios vía `shared` (vigilado por review).
* **Neutras**: la migración futura a polyrepo es mecánica porque cada servicio ya es un módulo Maven autocontenido.

## Validación

Revisión trimestral del criterio de salida contra métricas reales: número de personas activas, frecuencia de releases desincronizados, duración del CI.

## Referencias

* [ADR-0005](adr-0005-shared-como-starter-autoconfiguracion.md) — contenido y rol de `shared`
* [ADR-0002](adr-0002-microservicios-con-database-per-service.md) — límites de servicio
