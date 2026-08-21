# Alcance del Proyecto

* Tipo: documento de formalización · Audiencia: todas · Última actualización: 2026-08-20

---

## Alcance funcional v1 — Facturación electrónica

### Facturación electrónica clásica (Resolución 000042 de 2020)

- Emisión de factura electrónica de venta en estándar **UBL 2.1**.
- Generación y registro de **CUFE** por documento.
- Firma digital **XAdES-B** con certificado emitido para DIAN.
- Envío a los servicios web de la DIAN y gestión de acuses.
- Modo de **contingencia**: emisión offline con sincronización posterior.
- Notas crédito y débito.
- Entrega al cliente final: PDF + XML por email.

### Facturación POS (Resolución 000042 de 2020, anexo documentos equivalentes)

- Emisión de factura electrónica de venta POS.
- Generación y registro de **CUDE**.
- Flujo optimizado para caja: alta rotación, operación táctil, tolerancia a conectividad intermitente.

### Plataforma

- Gestión de usuarios, propietarios (owners) y empleados por negocio.
- Autenticación vía OAuth 2.0 (proveedor externo).
- API REST documentada con OpenAPI 3.x para integradores.
- Panel de administración para el operador pyme.

---

## Fuera de alcance v1

| Módulo | Estado | Nota |
|---|---|---|
| Contabilidad completa | Evolución futura | El roadmap lo contempla como módulo independiente |
| Nómina electrónica | Evolución futura | Requiere análisis normativo propio (Resolución 000024/2022+) |
| Inventario avanzado | Parcial | Lo mínimo para facturar POS entra; valorizaciones no |
| Multi-país | No contemplado | El dominio DIAN es la especialización central del proyecto |
| App móvil nativa | No contemplado | La experiencia POS se resuelve con web moderna |

---

## Servicios del sistema

La plataforma se compone de servicios independientes (ver [ADR-0002](../architecture/adr/adr-0002-microservicios-con-database-per-service.md)):

| Servicio | Responsabilidad | Estado |
|---|---|---|
| `user-service` | Usuarios, owners, empleados, datos de referencia | En desarrollo |
| `auth-service` | Identidad, tokens, sesiones | Planificado |
| `api-gateway` | Punto único de entrada: routing, rate limiting, auth | Planificado |
| `billing-service` | Emisión FE clásica: documentos UBL, CUFE, firma, DIAN | Planificado |
| `pos-service` | Flujo POS: alta rotación, contingencia, CUDE | Planificado |
| `notification-service` | Emails, plantillas, entrega de documentos | Planificado |
| `analytics-service` | Ingesta de eventos, ETL Python, reportes | Planificado |

Cada servicio posee su propia base de datos. La comunicación es síncrona (REST) donde la latencia importa y asíncrona (eventos Kafka) donde prima el desacoplamiento y la resiliencia.

## Restricciones regulatorias que condicionan el alcance

- Toda la normativa aplicable está centrada en la **DIAN** (Colombia): Resolución 000042 de 2020 y sus modificaciones, UBL 2.1, CUFE/CUDE, XAdES-B. Ver [`compliance/`](../compliance/README.md).
- Los datos personales (nombre, email, teléfono, identificación) son información sensible operativa: el sistema aplica cifrado a nivel de campo ([ADR-0028](../architecture/adr/adr-0028-cifrado-campo-indice-hmac-searchable.md)) desde el primer día, no como añadido posterior.

## Criterios de éxito v1

1. Un negocio real puede emitir su primera factura electrónica válida ante la DIAN de punta a punta.
2. El flujo POS soporta operación continua con conectividad degradada.
3. Cualquier desarrollador puede levantar el entorno local completo con Docker Compose y entender las decisiones arquitectónicas leyendo `docs/`.
