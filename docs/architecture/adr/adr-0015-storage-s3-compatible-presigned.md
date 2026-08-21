# ADR-0015: Storage S3-compatible con presigned URLs

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

Los usuarios del sistema tienen imágenes de perfil y, en el futuro, los documentos fiscales (PDF/XML facturados) necesitan almacenamiento de objetos. Los archivos no deben atravesar el backend en cada lectura: el backend debe firmar acceso temporal y el cliente descargar directo.

Había que elegir la tecnología de storage y el contrato de acceso.

## Impulsores de la decisión

* Compatibilidad S3 como estándar de facto (portabilidad local ↔ nube).
* Descarga directa cliente↔storage sin pasar por el servicio (escalado).
* Credenciales nunca expuestas al cliente.
* Entorno local reproducible para contribuyentes.

## Opciones consideradas

### Opción A: Storage en filesystem local del servicio

* Bien, porque cero dependencias.
* Malo, porque incompatible con múltiples réplicas; sin durabilidad; sin URLs firmadas.

### Opción B: MinIO para desarrollo + S3 en producción

* Bien, porque es la elección convencional del ecosistema.
* Malo, porque imagen pesada para el entorno local; el proyecto encontró una alternativa más ligera igualmente compatible.

### Opción C: Backend S3-compatible (RustFS local) + AWS SDK v2 con presigned URLs

Adaptador `FileStorageServiceAdapter` sobre AWS SDK v2: `upload`, `delete`, presigned GET de 15 minutos, utilidades `buildUrl`/`extractKeyFromUrl`. Puerto `FileStorageService` en dominio; autoconfiguración condicional en `shared`.

* Bien, porque cualquier endpoint S3-compatible funciona (RustFS hoy, S3/GCS/Others mañana) cambiando solo configuración; presigned URLs descargan el cliente directo; validación previa con ValidationChain antes de tocar el storage.
* Malo, porque RustFS es una elección poco común (menos batalla librada que MinIO — mitigado por la abstracción S3).

## Decisión

Opción elegida: **C**. Configuración bajo namespace `app.file-storage.*` (endpoint, bucket `jobby-bucket`). Errores mapeados a la taxonomía interna (`NoSuchBucketException` → `ITS_CONFIGURATION_ERROR`) ([ADR-0018](adr-0018-taxonomia-errores-tres-niveles.md)).

### Justificación

La API S3 es el único estándar real de storage de objetos. Elegir un backend compatible local mantiene el entorno ligero sin sacrificar portabilidad. Las presigned URLs resuelven el escalado de lectura sin infraestructura adicional (CDN después si hace falta).

## Consecuencias

* **Positivas**: portabilidad total de proveedor; lecturas fuera del hot path del backend; instrumentación `filestorage.*` integrada ([ADR-0013](adr-0013-observabilidad-self-hosted.md)).
* **Negativas**: relojes desincronizados pueden invalidar presigned URLs (ventana de 15 min tolerante); HealthIndicator propio necesario (no existe nativo) — pendiente en [backlog](../../internal/backlog.md).
* **Neutras**: el flujo de reemplazo de imagen usa el resultado del agregado (`ImageReplaceResult`) para borrar el objeto viejo tras confirmar el nuevo ([ADR-0019](adr-0019-fabrica-doble-of-reconstruct.md)).

## Validación

Tests unitarios del adapter con mocks del SDK; observaciones `filestorage.upload/get-signed/delete` en Grafana; verificación manual de URL firmada contra RustFS local.

## Referencias

* [Guía interna de observabilidad](../../internal/guides/observability-guide.md)
