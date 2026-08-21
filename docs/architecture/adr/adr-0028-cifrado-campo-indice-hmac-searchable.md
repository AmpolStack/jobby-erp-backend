# ADR-0028: Cifrado a nivel de campo con índice HMAC searchable

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

Jobby ERP maneja datos personales operativos (nombre, email, teléfono, número de identificación) cuya protección no es opcional: son PII de usuarios reales en un sistema fiscal. Cifrar "el disco" o "la conexión" no basta — cualquier lectura legítima de BD expondría texto plano. Pero cifrar todo crea un problema aparentemente imposible: **¿cómo se busca o se garantiza unicidad sobre un dato cifrado?** (un email cifrado con IV aleatorio produce ciphertexts distintos cada vez — imposible indexar).

Este es el mecanismo de protección de datos más importante del sistema y durante mucho tiempo existió *solo en código*, reconstruible leyendo `SecurityOrchestrator` y el script de inicialización de MongoDB.

## Impulsores de la decisión

* PII nunca en texto plano ni en reposo ni en logs/serialización.
* Búsqueda por igualdad y constraints UNIQUE sobre datos cifrados.
* Claves gestionadas como configuración validada, algoritmos estándar.
* API de uso simple para que ningún desarrollador "se salte" la seguridad.

## Opciones consideradas

### Opción A: Cifrado a nivel de columna/disco (TDE)

* Bien, porque transparente para la aplicación.
* Malo, porque cualquiera con acceso a la BD (admin, dump, backup) lee texto plano; no protege contra el vector real: acceso a datos.

### Opción B: Cifrar todo y renunciar a búsqueda/unicidad

* Bien, porque máxima simplicidad criptográfica.
* Malo, porque inviable operativamente: registrar un usuario exige verificar unicidad de email.

### Opción C: Cifrado determinista para todo

Cifrar con IV derivado del dato para permitir igualdad directa.

* Bien, porque búsqueda trivial.
* Malo, porque el cifrado determinista filtra igualdades a quien acceda a los ciphertexts (análisis de frecuencias); debilita AES-GCM.

### Opción ELEGIDA — Opción D: Cifrado probabilístico + índice HMAC separado

Dos representaciones por campo sensible:

| Política | Estructura | Uso |
|---|---|---|
| `ProtectedField` | `{data: byte[]}` — solo AES-GCM | Dato sensible sin necesidad de búsqueda |
| `IndexedField` | `{data: byte[] (AES-GCM), index: byte[] (HMAC-SHA256 determinista)}` | Dato sensible **con** búsqueda/unicidad |

- **AES/GCM/NoPadding** con IV aleatorio de 12 bytes prependido al ciphertext (probabilístico: mismo dato → ciphertexts distintos).
- **HMAC-SHA256 determinista** sobre el valor en claro → índice consultable; UNIQUE constraints de MongoDB viven sobre `.index` ([ADR-0011](adr-0011-mongodb-json-schema-indices-hmac.md)).
- `SecurityOrchestrator` con builder fluido y políticas: `secure().add(field, SECURED_ONLY_ENCRYPTION).add(other, SECURED).executeAll()` / `reverse()`.
- Triple protección del payload en claro: `transient` Java + `@Transient` Spring Data + `@JsonIgnore`.
- Builders criptográficos (`EncryptBuilder`, `MacBuilder`); `EncryptionService` y `MacService` como puertos de dominio; claves bajo `app.*` con validación en startup (`CryptoUtils.validateAndParseKey`, `validateConfig()`).

## Decisión

Opción elegida: **D**, aplicada sistemáticamente. Reglas complementarias:

1. Consultas de unicidad **nunca** comparan datos cifrados: siempre HMAC (`existByEmail` → `securityOrchestrator.index(email)` → query por `.index`).
2. Ningún campo protegido viaja a respuestas HTTP sin pasar por el mapper seguro (`SecuredFieldMapper` extrae payload descifrado).
3. La política (qué campo es Protected vs Indexed) se declara una vez por agregado en su orquestación.

### Justificación

Separa limpiamente las dos necesidades — confidencialidad (AES probabilístico) y buscabilidad (HMAC determinista) — sin sacrificar ninguna. El HMAC determinista expone igualdades (dos usuarios con mismo email → mismo índice), pero eso ya es conocido por la lógica de unicidad; el ciphertext permanece opaco incluso ante análisis de frecuencias. Es el equilibrio estándar de searchable encryption aplicable sin infraestructura especial (no requiere KMS externo hoy).

## Consecuencias

* **Positivas**: PII opaca ante dumps/backups/acceso directo a BD; unicidad real sobre cifrados; API fluida difícil de usar mal.
* **Negativas**: rotación de claves HMAC invalida índices (procedimiento de re-indexación pendiente de documentar); tamaño de documento mayor (data + index por campo).
* **Neutras**: la misma disciplina se extiende a mensajería ([ADR-0029](adr-0029-pii-cifrada-end-to-end-kafka.md)).

## Validación

Tests de roundtrip cifrado/descifrado y determinismo de HMAC ([ADR-0037](adr-0037-convenciones-tests.md)); verificación de que los índices únicos rechazan duplicados vía HMAC; skill `security-sensitive-data` planificado ([ADR-0038](adr-0038-skills-opencode-guardianes.md)).

## Referencias

* [Guía interna de seguridad](../../internal/guides/security-guide.md)
* [Patrón público: Searchable Encryption](../patterns/searchable-encryption.md)
