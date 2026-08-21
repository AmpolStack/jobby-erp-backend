# Visión y Objetivos

* Tipo: documento de formalización · Audiencia: todas · Última actualización: 2026-08-20

---

## Misión

Resolver el problema de la facturación electrónica para las pymes colombianas con una plataforma open source construida sobre microservicios independientes: diseñada para alta disponibilidad y aislamiento de fallas, y sobre todo fácil de aprender, intuitiva y visualmente moderna.

## Visión

Ser la referencia latinoamericana de cómo construir un producto SaaS real con arquitectura de microservicios — donde el código, las decisiones y los patrones estén tan bien documentados que el propio repositorio funcione como material de estudio de sistemas distribuidos.

## El problema

La mayoría de soluciones de facturación en Colombia comparten los mismos problemas de fondo:

1. **Arquitecturas monolíticas** que colapsan bajo carga.
2. **Interfaces que complican operaciones básicas** que un cajero hace decenas de veces al día.
3. **Sistemas que se caen justo cuando más se necesitan** — fin de mes, temporadas altas, cierre fiscal.

Jobby ERP aborda estos tres problemas desde cero, no como parches sobre un producto existente.

## Objetivos

### Objetivo general

Crear una aplicación completa, viable y lista para producción que resuelva la facturación electrónica colombiana (clásica y POS) para pymes — una plataforma diseñada para no caerse nunca.

### Objetivos específicos

**1. Mejores prácticas en microservicios, documentadas para aprender.**
Implementar un producto real que aplique patrones estándar de la industria — CQRS, Database per Service, Outbox, Result Pattern, eventos de dominio, entre otros — produciendo documentación técnica detallada que explique cómo funciona cada patrón, por qué se eligió, sus trade-offs y cómo se manifiesta en un codebase real. Ver [principios](principles.md) y [ADRs](../architecture/adr/README.md).

**2. Un producto con adopción real.**
Más allá de ser un proyecto de referencia, Jobby ERP está diseñado para ser adoptado por negocios reales. Las decisiones arquitectónicas, de UX y de priorización se toman pensando en operadores reales de pymes colombianas.

**3. Open source como valor de primera clase.**
El proyecto se construye en público y permanecerá open source independiente de su trayectoria comercial. La intención es demostrar que open source y calidad de producto no están en tensión.

---

## Público objetivo

| Actor | Relación con el producto |
|---|---|
| Dueño de negocio / operador pyme | Configura la plataforma, gestiona usuarios, monitorea facturación, revisa reportes |
| Contador / cajero | Emite facturas electrónicas clásicas y POS en la operación diaria |
| Desarrollador / integrador | Conecta sistemas externos vía REST API |
| Desarrollador estudiante | Estudia el repositorio como caso real de microservicios |

## Qué NO es este proyecto

- **No es un ERP completo (aún).** El alcance v1 es facturación electrónica; módulos contables o de nómina son evolución futura (ver [roadmap](roadmap.md)).
- **No es un framework ni una librería general.** El módulo `shared` existe para los servicios de Jobby; su reutilización externa es un efecto, no el objetivo.
- **No es un tutorial desacoplado del código.** Toda la documentación describe decisiones implementadas en este repositorio; si el código cambia, la documentación lo sigue.
