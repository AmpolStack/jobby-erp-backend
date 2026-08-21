# Roadmap Técnico

* Tipo: documento de formalización · Audiencia: todas · Última actualización: 2026-08-20
* Naturaleza: documento vivo — se actualiza conforme avanza el proyecto. El detalle operativo vive en [issues](https://github.com/AmpolStack/jobby-erp-backend/issues).

---

## Estado actual (Fase 0 — Fundamentos)

**Completado / en curso:**

- ✅ Módulo `shared`: Result Pattern, ValidationChain, taxonomía de errores, buses in-process, TransactionOrchestrator, cifrado a nivel de campo con índices HMAC, autoconfiguraciones estilo starter.
- ✅ Infraestructura local Docker Compose: Kafka 4.0 KRaft (1+3), Apicurio Registry, Redis, MongoDB 8, RustFS (S3), Mailpit, stack de observabilidad (Prometheus + Loki + Zipkin + Grafana).
- 🔄 `user-service`: casos de uso de owners/users, eventos de dominio con `pullDomainEvents()`, publicación Kafka con Avro + Apicurio, cambio de email con códigos efímeros.
- ✅ Sistema documental: ADRs MADR, guías internas, patrones públicos, estrategia de contenido.

## Fase 1 — Identidad y borde

- `auth-service`: emisión/validación de tokens OAuth 2.0, sesiones, refresh.
- `api-gateway`: routing, rate limiting, enforcement de auth en el borde.
- Endpoints pendientes de `user-service` (ver [backlog](../internal/backlog.md)).
- Tests de integración con Testcontainers para bordes de infraestructura.

## Fase 2 — Núcleo de facturación

- `billing-service`: modelo de documentos UBL 2.1, generación CUFE, firma XAdES-B.
- Integración con servicios web DIAN (habilitación + producción).
- Outbox Pattern completo ([ADR-0024](../architecture/adr/adr-0024-outbox-pattern-atomicidad-bd-kafka.md)) como garantía de entrega de eventos críticos.
- Notas crédito/débito.

## Fase 3 — POS y contingencia

- `pos-service`: flujo de alta rotación, CUDE.
- Modo contingencia offline con sincronización posterior.
- Experiencia de caja tolerante a conectividad intermitente.

## Fase 4 — Notificaciones y analítica

- `notification-service`: plantillas, entrega PDF/XML, reintentos.
- `analytics-service`: ingesta de eventos, ETL Python, OLAP.
- Reportes financieros para el operador pyme.

## Fase 5 — Hardening de producción

- Kubernetes (despliegue, autoscaling, health probes).
- CDC con Debezium evaluado contra Outbox relay (la infraestructura ya lo anticipa).
- Gestión de secretos externalizada (hoy es deuda conocida: ver [backlog](../internal/backlog.md)).
- Observabilidad afinada: SLOs por servicio, alertas.

---

## Principios del roadmap

1. **Cada fase deja valor observable**: no avanzamos a la siguiente sin algo funcionando punta a punta.
2. **Los patrones se estrenan donde más duelen**: Outbox entra con billing porque ahí la pérdida de un evento tiene consecuencias legales/fiscales.
3. **La documentación acompaña, no persigue**: cada feature nueva nace con su ADR/guía (principio 8 de [principles.md](principles.md)).
