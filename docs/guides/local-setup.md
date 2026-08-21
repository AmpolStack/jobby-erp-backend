# Guía de Setup Local

* Tipo: guía · Audiencia: contribuyentes y evaluadores · Última actualización: 2026-08-20

---

## Prerrequisitos

| Herramienta | Versión | Uso |
|---|---|---|
| JDK | 21+ | Compilar y ejecutar servicios |
| Docker + Compose v2 | reciente | Infraestructura completa |
| Maven | 3.9+ (o usar wrapper `mvnw`) | Build multi-módulo |
| Git | — | Clonar el repo |

## 1. Levantar la infraestructura

Todos los archivos de infraestructura viven en [`jobby/shared/docker/`](../../jobby/shared/docker/). El compose principal incluye los otros dos:

```bash
cd jobby/shared/docker
docker compose -f docker-compose.yml up -d
```

Esto levanta:

| Grupo | Servicios | Puertos expuestos |
|---|---|---|
| **Base** ([docker-compose.yml](../../jobby/shared/docker/docker-compose.yml)) | RustFS (S3), Redis 7.4, Mailpit (SMTP dev) | 9000/9001, 6379, 1025/8025 |
| **Mensajería** ([docker-compose-messaging.yml](../../jobby/shared/docker/docker-compose-messaging.yml)) | Kafka 4.0 KRaft (1 controller + 3 brokers), Apicurio Registry 3.3, Kafka UI | 9092–9095, 8091, 8092 |
| **Observabilidad** ([docker-compose.monitoring.yml](../../jobby/shared/docker/docker-compose.monitoring.yml)) | Prometheus, Loki, Zipkin, Grafana (dashboard pre-provisionado) | 9090, 3100, 9411, 3000 |

> **Nota:** MongoDB se levanta aparte con su init script (`user-service/docker/sources/mongo/mongo-init-script.js`) que crea el JSON Schema estricto y los índices UNIQUE sobre campos HMAC. Verificar el comando exacto en ese directorio.

Las credenciales de desarrollo están en `jobby/shared/docker/.env` (solo para entorno local — ver [backlog](../internal/backlog.md) sobre externalización de secretos).

## 2. Compilar el monorepo

```bash
cd jobby
./mvnw clean install          # compila shared + user-service
```

El módulo `shared` produce un starter con autoconfiguraciones (buses, seguridad, observabilidad) y un test-jar reutilizable.

## 3. Ejecutar user-service

```bash
cd jobby/user-service
../mvnw spring-boot:run
```

Verificación rápida:

| URL | Qué esperar |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Swagger UI del servicio |
| `http://localhost:8080/actuator/health` | Estado de componentes |
| `http://localhost:8080/actuator/prometheus` | Métricas exportadas |
| `http://localhost:3000` | Grafana (dashboard Spring Boot Observability) |
| `http://localhost:8092` | Kafka UI (tópicos, esquemas Apicurio) |
| `http://localhost:8025` | Mailpit (emails de bienvenida en dev) |

## 4. Ejecutar los tests

```bash
cd jobby
./mvnw test                   # todos los módulos
./mvnw test -pl user-service  # solo user-service
```

Convenciones de testing: [`docs/internal/guides/testing-conventions.md`](../internal/guides/testing-conventions.md).

## Problemas comunes

| Síntoma | Causa probable |
|---|---|
| Kafka no arranca / topics no creados | El contenedor `kafka-init` debe completarse antes que Apicurio; revisar `docker logs kafka-init` |
| Esquemas no encontrados al publicar | Apicurio usa storage kafkasql: si se borran volúmenes de Kafka sin los del registry, quedan desincronizados — `docker compose down -v` y volver a subir |
| Emails no llegan | Mailpit no envía correos reales; verlos en su UI (`:8025`) |
| Health DOWN por Redis/S3 | Verificar que `.env` cargó (`docker compose config`) |

## Estado de esta guía

Esta guía cubre la infraestructura compartida y `user-service`. Se ampliará conforme nuevos servicios entren al [roadmap](../project/roadmap.md).
