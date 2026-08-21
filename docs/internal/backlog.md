# Backlog Técnico — Deudas Conocidas

* Tipo: registro interno · Última actualización: 2026-08-20

Deudas técnicas detectadas durante la formalización documental (análisis de código 2026-08). Cada ítem indica dónde vive y qué lo resuelve. Este archivo no reemplaza issues: los ítems que se activen deben tener su issue en GitHub.

---

## Prioridad P1

| Deuda | Ubicación | Notas |
|---|---|---|
| Tests de capa application (0 actualmente) | `user-service/application/` | Casos concretos: `CreateOwnerUseCaseAdapterTest` (éxito + duplicados), `UpdateEmailUseCaseAdapterTest`, `UpdateRecoveryEmailUseCaseAdapterTest` — Mockito sobre repositorios/transacción |
| Crear los 11 skills OpenCode | `.opencode/skills/` | Especificados en [guía](guides/opencode-skills.md); decisión [ADR-0038](../architecture/adr/adr-0038-skills-opencode-guardianes.md) |
| Claves AES/HMAC y credenciales en claro en yaml/.env versionados | `application.yaml`, `docker/.env` | Aceptado para desarrollo local; externalizar secretos antes de producción (Fase 5 roadmap) |
| Endpoints sin implementar o eliminar | `ContactController`, `UserController.update/updateStatus`, `OwnerController.updateSecurityParams` | Decidir: implementar o eliminar; `EmployeeRepository` sin uso |

## Prioridad P2

| Deuda | Ubicación | Notas |
|---|---|---|
| Timeout hardcodeado (5s) en publishes Kafka | `EventPublisherAdapter`, `KafkaTransactionalEventPublisher` | Externalizar a configuración |
| `mockito-inline` 4.11.0 legacy junto a Mockito 5 | POMs | Unificar a Mockito 5 con configuración inline nativa |
| Mutaciones de agregado sin `Result` | `User.updateEmail()` usa `if` manual | Patrón correcto ya aplicado en `Owner.removeRecoveryEmail()` |
| `Owner.create()` no valida con ValidationChain | `Owner.java` | Inconsistencia vs `User.create()` (valida 7 not-nulls); `secureParameters` puede ser null |
| HealthIndicators SMTP/S3 | `shared/.../health/` | No existen nativos; crear condicionales ([guía observabilidad](guides/observability-guide.md)) |
| Métricas de negocio pendientes | Use cases | Catálogo definido en [ADR-0013](../architecture/adr/adr-0013-observabilidad-self-hosted.md) |
| Instrumentación Observation faltante en algunos adapters | Redis/Kafka/Email/S3/Mongo | Checklist en [guía observabilidad](guides/observability-guide.md) |
| Tests de integración con Testcontainers | Todos los servicios | Infra compose ya existe; validar validators Mongo, índices HMAC, roundtrip Kafka |

## Prioridad P3

| Deuda | Ubicación | Notas |
|---|---|---|
| Generación de ID dentro del caso de uso/adapters | `idGenerator.next()` en adapters | Propuesta: mover al agregado (`User.create()` sin ID + asignación posterior) — en análisis |
| Overfetching en respuestas | `UpdateEmailUseCase.confirm()` retorna UserResponse completo | Debería retornar DTO mínimo (`EmailChangeConfirmedResponse`) |
| Obsesión primitiva puntual | `identificationTypeId: int`, `positionName: String` | Encapsular en VOs — aplazado por decisión explícita |
| `Result.mapError` marcado Legacy | `shared/domain/mobility/result/` | Evaluar eliminación |
| `GenericUnimplementedResponse` en controllers | `infrastructure/adapters/in/rest/` | Limpiar cuando endpoints se decidan |
| Autorización pendiente | `UserController.updateStatus` (`TODO: Move to logic with permissions`) | Bloquea hasta auth-service (Fase 1) |
| Duplicación Avro↔DomainEvent | Transformaciones manuales por evento | Simplificación deseable, no urgente |
| Rotación de claves HMAC sin procedimiento | Seguridad | Documentar re-indexación ([ADR-0028](../architecture/adr/adr-0028-cifrado-campo-indice-hmac-searchable.md)) |

---

## Cómo usar este backlog

1. Al planificar una fase del [roadmap](../project/roadmap.md), revisar qué deudas bloquean o complementan.
2. Al activar un ítem: crear issue referenciando esta fila + el ADR/guía correspondiente.
3. Al resolverlo: eliminar la fila aquí (el historial vive en git).
