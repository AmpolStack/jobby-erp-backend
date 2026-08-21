# Guía de Seguridad — Cifrado de Campo y PII

* Tipo: guía interna (how-to) · Audiencia: contribuyentes · Última actualización: 2026-08-20
* Decisiones subyacentes: [ADR-0028](../../architecture/adr/adr-0028-cifrado-campo-indice-hmac-searchable.md), [ADR-0029](../../architecture/adr/adr-0029-pii-cifrada-end-to-end-kafka.md), [ADR-0030](../../architecture/adr/adr-0030-sanitizacion-errores-support-id.md), [ADR-0035](../../architecture/adr/adr-0035-codigos-efimeros-redis.md)

> Este mecanismo existió mucho tiempo solo en código. Esta guía lo hace explícito.

---

## El modelo mental

Cada campo sensible tiene **dos representaciones** según su política:

```
                    ┌─ ProtectedField ── { data: AES-GCM }                        → solo confidencialidad
valor en claro ─────┤
                    └─ IndexedField ──── { data: AES-GCM, index: HMAC-SHA256 }    → confidencialidad + búsqueda
```

| Componente | Algoritmo | Propósito |
|---|---|---|
| `data` | AES/GCM/NoPadding, IV aleatorio 12B prependido, tag GCM 128 bits | Confidencialidad probabilística (mismo dato → ciphertexts distintos) |
| `index` | HMAC-SHA256 determinista | Búsqueda por igualdad + UNIQUE constraints |

---

## SecurityOrchestrator

Builder fluido con políticas declaradas una vez por agregado:

```java
// Cifrar al persistir
orchestrator.secure()
    .add(emailField, StorageSecurityPolicy.SECURED)              // cifrado + índice
    .add(recoveryEmailField, StorageSecurityPolicy.SECURED_ONLY_ENCRYPTION)  // solo cifrado
    .executeAll();

// Descifrar al leer
orchestrator.reverse().executeAll();
```

Estructuras (`shared/security/fields/`):

- `IndexedField` = `{data: byte[], index: byte[], payload transient}`.
- `ProtectedField` = `{data: byte[]}`.

**Triple protección del payload en claro:** `transient` Java + `@Transient` Spring Data + `@JsonIgnore` — imposible que fugue por serialización, logs o persistencia accidental.

---

## Reglas operativas

### 1. Unicidad SIEMPRE por HMAC, nunca por dato cifrado

```java
// En el repository adapter
public Result<Boolean, Error> existByEmail(Email email) {
    var index = securityOrchestrator.index(email);   // HMAC determinista
    // query por igualdad contra el campo .index
}
```

Los UNIQUE constraints de MongoDB viven sobre `.index`:

```javascript
{ "email.index": 1 }  { unique: true }
{ "identification_number.index": 1 }  { unique: true }
{ "phone.index": 1 }  { unique: true }
```

### 2. Mapeo seguro hacia HTTP

`SecuredFieldMapper` (con qualifiers `@Named`) extrae el payload descifrado de `IndexedField`/`ProtectedField` → String. Ningún campo protegido llega a una respuesta sin pasar por aquí.

### 3. PII en Kafka también viaja cifrada

Los esquemas Avro declaran campos sensibles como `bytes`; la Transformation cifra antes de serializar ([ADR-0029](../../architecture/adr/adr-0029-pii-cifrada-end-to-end-kafka.md)). Ver [guía de mensajería](messaging-guide.md).

### 4. Claves validadas en startup

- Configuración bajo namespace `app.*` (`EncryptConfig`, `MacConfig`).
- `CryptoUtils.validateAndParseKey()` + `EncryptionCryptography.validateConfig()` fallan al arrancar si la clave es inválida.
- El servicio runtime (`AESEncryptionService`) no re-valida: la config ya es confiable ([ADR-0027](../../architecture/adr/adr-0027-validacion-distribuida-por-capas.md)).

---

## Flujos sensibles con códigos efímeros

Patrón para cambio de email (y replicable a recuperación de contraseña, OTP):

1. Código de 8 caracteres con `SecureRandom`.
2. Guardar `{code, newEmail}` en Redis bajo `user.change-email.{userId}`, TTL 5 min.
3. `validate()` compara **y elimina la clave** — consumo único garantizado.

Ver [ADR-0035](../../architecture/adr/adr-0035-codigos-efimeros-redis.md).

---

## Sanitización de errores

Los errores internos (`ITN_*`, `ITS_*`) se sanitizan antes de exponerse; las respuestas Problem Details incluyen `supportId` = traceId para correlación de soporte. Detalles en [ADR-0030](../../architecture/adr/adr-0030-sanitizacion-errores-support-id.md) y la [guía de validación](validation-guide.md).

---

## Deuda de seguridad conocida

Las claves AES/HMAC están hoy en claro dentro de `application.yaml` y `.env` versionados. Es deuda aceptada para desarrollo local con plan de externalización (secretos) en Fase 5 del roadmap. Ver [backlog](../backlog.md).

---

## Checklist para un campo sensible nuevo

- [ ] ¿Necesita búsqueda/unicidad? → `IndexedField`; si no → `ProtectedField`
- [ ] ¿Política declarada en el orquestador del agregado?
- [ ] ¿Índice único sobre `.index` creado en el script de init?
- [ ] ¿El mapper HTTP usa `SecuredFieldMapper`?
- [ ] ¿Si viaja en eventos? → `bytes` cifrado en el `.avsc`
- [ ] ¿Tests de roundtrip cifrado/descifrado existen?
