# ADR-0003: DDD + Arquitectura Hexagonal con dominio puro

* Fecha: 2026-08-20 (retro-documentada)
* Estado: aceptada

## Contexto y problema

Cada servicio de Jobby ERP necesita una estructura interna que proteja la lógica de negocio de los detalles técnicos (framework, BD, mensajería). La facturación electrónica es un dominio normativo denso — reglas DIAN, UBL, CUFE — que cambiará por razones regulatorias, mientras la infraestructura cambia por razones técnicas. Mezclar ambos ejes de cambio en las mismas clases hace el sistema frágil e inestudiable.

## Impulsores de la decisión

* El dominio normativo (DIAN/UBL) cambia por razones ajenas a la tecnología.
* Testabilidad: la lógica de negocio debe probarse sin levantar Spring ni BD.
* Objetivo educativo: que las fronteras arquitectónicas sean visibles y enseñables.
* Regla verificable, no aspiracional: "dominio puro" debe poder auditarse.

## Opciones consideradas

### Opción A: Arquitectura en capas clásica (controller → service → repository)

* Bien, porque es familiar y rápida de empezar.
* Malo, porque la lógica termina filtrándose hacia controllers/repositories; el modelo queda subordinado al ORM y al framework.

### Opción B: Clean Architecture genérica

Entidades + casos de uso + adaptadores con capa de frameworks externa.

* Bien, porque separa responsabilidades correctamente.
* Malo, porque su vocabulario abstracto (entities, use cases interactor) no aprovecha el modelado táctico de DDD, valioso para un dominio normativo como facturación.

### Opción C: DDD táctico + Hexagonal (puertos y adaptadores)

Dominio puro en el centro (`domain/`), casos de uso orquestando en `application/`, adaptadores en `infrastructure/`. Puertos como interfaces puras definidas en dominio; dependencias solo hacia adentro.

* Bien, porque el centro no conoce Spring ni infraestructura (cero imports); los puertos expresan intención de negocio; cada capa tiene reglas de dependencia explícitas y auditables.
* Malo, porque exige más estructura inicial (más archivos por feature) y disciplina constante contra fugas de framework hacia el dominio.

## Decisión

Opción elegida: **C — DDD táctico + Hexagonal** con dominio puro como regla inviolable:

```
infrastructure/adapters/in/rest ──► application/useCases ──► domain/ports/in
infrastructure/adapters/out/*  ◄── implementa ◄── domain/ports/out
domain/  → cero imports de infrastructure/, Spring o librerías externas
           (excepción única: Lombok y java.*)
```

Reglas operativas completas en [`internal/guides/architecture-rules.md`](../../internal/guides/architecture-rules.md).

### Justificación

El dominio fiscal colombiano es exactamente el tipo de problema para el que DDD existe: reglas complejas, cambiantes por normativa, con vocabulario propio. Hexagonal aporta la mecánica (puertos/adaptadores) que hace la pureza del dominio *estructuralmente forzada* en lugar de dependiente de buena voluntad.

## Consecuencias

* **Positivas**: dominio testeable sin framework; cambios regulatorios aislados del código técnico; fronteras visibles para contribuyentes; VOs y agregados como lenguaje común.
* **Negativas**: más ceremonia por feature (puerto + adapter + contratos); riesgo real de anemia si los agregados no absorben comportamiento (deuda documentada y vigilada).
* **Neutras**: `application/` actúa como única "zona gris" controlada donde se permiten anotaciones Spring como metadatos.

## Validación

Code review verifica imports de `domain/` en cada PR; skills automatizados planificados lo harán mecánico ([ADR-0038](adr-0038-skills-opencode-guardianes.md)). Los agregados anémicos conocidos están registrados como deuda en [`internal/backlog.md`](../../internal/backlog.md).

## Referencias

* [Guía de reglas de arquitectura](../../internal/guides/architecture-rules.md)
* [Patrón público: Hexagonal + DDD](../patterns/hexagonal-ddd.md)
* Evans, E. *Domain-Driven Design* · Vernon, V. *Implementing Domain-Driven Design*
