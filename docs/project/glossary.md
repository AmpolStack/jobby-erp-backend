# Glosario

* Tipo: documento de formalización · Audiencia: todas · Última actualización: 2026-08-20

Dos vocabularios conviven en este proyecto: el **dominio de facturación colombiana** (normativo) y el **vocabulario técnico** (arquitectura y patrones). Este glosario los unifica para que contribuyentes de cualquier origen puedan leer cualquier documento sin fricción.

---

## Dominio — Facturación electrónica colombiana

| Término | Definición |
|---|---|
| **DIAN** | Dirección de Impuestos y Aduanas Nacionales. Autoridad tributaria colombiana; recibe, valida y registra todos los documentos electrónicos vía sus servicios web oficiales. |
| **FE** | Facturación Electrónica. |
| **Resolución 000042 de 2020** | Norma DIAN que define los requisitos técnicos de la facturación electrónica (formatos, validaciones, plazos). |
| **UBL 2.1** | Universal Business Language 2.1. Estándar OASIS de esquemas XML usado por la DIAN para todos los documentos electrónicos. |
| **CUFE** | Código Único de Factura Electrónica. Identificador único generado con SHA-384 para cada factura electrónica clásica. |
| **CUDE** | Código Único de Documento Electrónico. Equivalente del CUFE para documentos electrónicos equivalentes (incluye factura POS). |
| **XAdES-B** | XML Advanced Electronic Signatures (forma B). Firma digital XML obligatoria sobre cada documento, con certificado digital emitido por autoridad certificadora habilitada. |
| **Documento electrónico equivalente (DEE)** | Documentos distintos a la factura estándar con efectos fiscales: factura POS, tiquete, nota contable, entre otros. |
| **Modo contingencia** | Régimen que permite emitir documentos sin conexión a la DIAN (fallas técnicas o zona sin cobertura), con sincronización posterior dentro de plazos normativos. |
| **Habilitación / Producción** | Las dos fases del operador ante la DIAN: primero se valida el sistema en ambiente de pruebas (habilitación), luego opera ante el mundo real (producción). |
| **Pyme** | Pequeña y mediana empresa colombiana — el público objetivo del producto. |

## Vocabulario técnico — Arquitectura

### DDD y Hexagonal

| Término | Definición |
|---|---|
| **Aggregate (Agregado)** | Unidad de consistencia transaccional del dominio. Se persiste completo o nada; sus entidades hijas solo se modifican a través de la raíz (`Owner.addContact(...)`, nunca tocando la lista directamente). |
| **Aggregate Root (Raíz de agregado)** | Punto único de entrada al agregado (`User`, `Owner`, `Employee`). |
| **Value Object (VO)** | Objeto inmutable sin identidad, definido por sus atributos, autovalidado en construcción (`Email`, `Phone`, `Name`). Preferimos `record` cuando no hay lógica compleja. |
| **Puerto (Port)** | Interfaz pura definida en dominio. *Inbound*: casos de uso (`ports/in`). *Outbound*: repositorios y servicios externos (`ports/out`). Sin anotaciones de framework. |
| **Adaptador (Adapter)** | Implementación de un puerto en infraestructura (REST, MongoDB, Kafka, S3). |
| **Caso de uso (Use Case)** | Orquestador delgado: `load → validate → aggregate.behavior() → save → notify`. Nunca genera IDs ni valida campo a campo. |
| **Bounded Context** | Frontera de modelo. `User` y `Owner` viven en contextos distintos aunque representen personas relacionadas ([ADR-0033](../architecture/adr/adr-0033-colecciones-separadas-users-owners-employees.md)). |
| **Rich vs Anemic Domain Model** | Rico: lógica de negocio dentro del agregado. Anémico: solo datos + factories. Toleramos anemia temporal documentada; la dirección es hacia rico. |

### Manejo de errores y validación

| Término | Definición |
|---|---|
| **Result Pattern** | Todo flujo de negocio retorna `Result<T, Error>` explícito (sealed interface con `Success`/`Failure`), compuesto con `map/flatMap/peek/fold`. Prohibido lanzar excepciones por reglas de negocio ([ADR-0016](../architecture/adr/adr-0016-result-pattern-unico.md)). |
| **ValidationChain** | Cadena de validaciones perezosas con cortocircuito: no ejecuta nada hasta `build()`; la primera falla detiene todo ([ADR-0017](../architecture/adr/adr-0017-validationchain-perezosa.md)). |
| **ErrorType** | Enum con taxonomía de tres niveles codificada por prefijo: sin prefijo = visible al usuario; `ITN_*` = error interno de validación; `ITS_*` = error de sistema/configuración ([ADR-0018](../architecture/adr/adr-0018-taxonomia-errores-tres-niveles.md)). |
| **Sanitización** | Reemplazo del detalle real de un error interno por mensajes genéricos antes de exponerlo al cliente ([ADR-0030](../architecture/adr/adr-0030-sanitizacion-errores-support-id.md)). |
| **Fábrica doble `of()`/`reconstruct()`** | Convención universal: `of()` valida y retorna `Result`; `reconstruct()` hidrata desde fuente confiable (BD) sin validar ni emitir eventos ([ADR-0019](../architecture/adr/adr-0019-fabrica-doble-of-reconstruct.md)). |

### Mensajería y eventos

| Término | Definición |
|---|---|
| **Command / Query / Event** | Contratos como records inmutables. Command: intención que muta (1 handler). Query: pregunta que lee (1 handler). Event: hecho ocurrido (N handlers, fire-and-forget) ([ADR-0020](../architecture/adr/adr-0020-buses-in-process-command-query-event.md), [ADR-0021](../architecture/adr/adr-0021-contratos-records-application.md)). |
| **Bus in-process** | Despachador en la misma JVM que resuelve handlers por tipo de contrato, con registro explícito (sin reflexión) y decoradores de logging/métricas. |
| **DomainEvent** | Hecho de negocio publicado hacia afuera del servicio (Kafka). Distinto del `Event` in-process ([ADR-0023](../architecture/adr/adr-0023-separacion-event-interno-domainevent-externo.md)). |
| **`pullDomainEvents()`** | Estrategia de emisión: el agregado acumula eventos internamente y el caso de uso los "ordeña" después de la transacción exitosa ([ADR-0022](../architecture/adr/adr-0022-eventos-dominio-pull-domain-events.md)). |
| **Outbox Pattern** | Persistir el evento en la misma transacción de BD y publicarlo después mediante relay, garantizando atomicidad BD-mensajería ([ADR-0024](../architecture/adr/adr-0024-outbox-pattern-atomicidad-bd-kafka.md)). |
| **DLT (Dead Letter Topic)** | Topic `.DLT` donde caen mensajes que fallan tras reintentos con backoff exponencial, para inspección manual. |
| **KRaft** | Protocolo propio de Kafka para consenso sin ZooKeeper (usado desde Kafka 4.0). |
| **Schema Registry** | Servicio centralizado de versionamiento de esquemas de serialización. Usamos Apicurio con storage kafkasql ([ADR-0009](../architecture/adr/adr-0009-apicurio-wire-format-confluent.md)). |
| **Wire format** | Estructura binaria del mensaje serializado: `[magic byte][ID de schema][payload Avro]`. Mantenemos compatibilidad con el formato Confluent de facto. |
| **Avro** | Formato de serialización binaria con esquemas versionados; clases generadas en build-time desde archivos `.avsc` ([ADR-0010](../architecture/adr/adr-0010-avro-build-time-eventos.md)). |

### Seguridad

| Término | Definición |
|---|---|
| **Cifrado a nivel de campo** | Cada dato sensible se cifra individualmente (AES-GCM) antes de persistir, no solo el disco o la conexión ([ADR-0028](../architecture/adr/adr-0028-cifrado-campo-indice-hmac-searchable.md)). |
| **Índice HMAC searchable** | HMAC-SHA256 determinista del valor en claro, almacenado junto al cifrado, que permite búsqueda por igualdad y constraints UNIQUE sobre datos cifrados. |
| **ProtectedField / IndexedField** | Políticas del SecurityOrchestrator: solo cifrado vs cifrado + índice HMAC. |
| **PII** | Personally Identifiable Information — datos personales operativos (nombre, email, teléfono, identificación). Viaja cifrada incluso dentro de Kafka ([ADR-0029](../architecture/adr/adr-0029-pii-cifrada-end-to-end-kafka.md)). |
| **supportId** | traceId incluido en respuestas Problem Details para correlacionar un error reportado por el usuario con su traza distribuida ([ADR-0030](../architecture/adr/adr-0030-sanitizacion-errores-support-id.md)). |

### Infraestructura

| Término | Definición |
|---|---|
| **Database per Service** | Cada microservicio posee su esquema; ninguna BD compartida entre servicios ([ADR-0002](../architecture/adr/adr-0002-microservicios-con-database-per-service.md)). |
| **Starter / Autoconfiguración** | Módulo `shared` configura beans condicionalmente (`@ConditionalOnClass` + `@ConditionalOnMissingBean`) permitiendo override por servicio ([ADR-0005](../architecture/adr/adr-0005-shared-como-starter-autoconfiguracion.md)). |
| **TransactionOrchestrator** | Orquestador de transacciones explícitas: repositorios devuelven `PersistenceTask` diferidas; el caso de uso las compone y confirma juntas ([ADR-0025](../architecture/adr/adr-0025-transacciones-explicitas-transaction-orchestrator.md)). |
| **Snowflake ID** | Generador de IDs únicos distribuidos (64 bits: timestamp + worker + secuencia) ([ADR-0014](../architecture/adr/adr-0014-snowflake-ids-distribuidos.md)). |
| **Presigned URL** | URL firmada temporal (15 min) para acceso directo a objetos S3 sin pasar credenciales ([ADR-0015](../architecture/adr/adr-0015-storage-s3-compatible-presigned.md)). |
| **Micrometer Observation** | API única que produce métricas (Prometheus) y spans (Zipkin) desde una sola envoltura de código ([ADR-0013](../architecture/adr/adr-0013-observabilidad-self-hosted.md)). |
| **Problem Details** | RFC 7807: formato estándar JSON para errores HTTP (`type`, `title`, `status`, `detail`) ([ADR-0031](../architecture/adr/adr-0031-problem-details-rfc7807.md)). |
