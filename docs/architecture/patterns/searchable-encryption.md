# Searchable Encryption — PII cifrada pero consultable

* Guía educativa · Decisiones: [ADR-0028](../adr/adr-0028-cifrado-campo-indice-hmac-searchable.md), [ADR-0029](../adr/adr-0029-pii-cifrada-end-to-end-kafka.md) · Guía operativa: [internal/guides/security-guide.md](../../internal/guides/security-guide.md)

---

## El problema

Un ERP de facturación vive de datos personales: nombres, emails, teléfonos, números de identificación. Cifrarlos "en reposo" (disco, TDE) no protege contra el vector real: cualquiera con acceso a la BD — un admin, un dump, un backup filtrado — lee texto plano.

La solución obvia es cifrar cada campo en la aplicación. Y ahí aparece la paradoja:

> El email se cifra con AES-GCM usando un IV aleatorio → el mismo email produce ciphertexts **distintos** cada vez → ¿cómo garantizo unicidad? ¿cómo busco por email?

Cifrar todo y renunciar a búsqueda es inviable para un sistema que registra usuarios. Cifrar deterministamente (IV derivado del dato) permite igualdad directa pero filtra patrones a quien vea los ciphertexts (análisis de frecuencias).

## La idea central

Separar las dos necesidades en dos representaciones del mismo campo:

```
                    ┌─ data:  AES-GCM(IV aleatorio)  → confidencialidad
"ana@ejemplo.co" ───┤
                    └─ index: HMAC-SHA256(clave)      → búsqueda/unicidad
```

- **`data`** (probabilístico): opaco incluso ante análisis de frecuencias. Nadie deduce nada comparando ciphertexts.
- **`index`** (determinista): el HMAC produce siempre el mismo valor para la misma entrada → sirve como clave de búsqueda y soporta `UNIQUE` constraints.

```javascript
// MongoDB: la unicidad vive sobre el índice HMAC, nunca sobre el dato
db.users.createIndex({ "email.index": 1 }, { unique: true })
```

```java
// La consulta de unicidad compara HMACs, jamás ciphertexts
var index = securityOrchestrator.index(email);
// SELECT ... WHERE email.index = :index
```

## Lo que lo hace robusto en este codebase

1. **API difícil de usar mal**: `SecurityOrchestrator.secure().add(campo, POLICY).executeAll()` — la política se declara una vez; el desarrollador no maneja criptografía directamente.
2. **Triple protección del payload en claro**: `transient` Java + `@Transient` Spring Data + `@JsonIgnore` — imposible que fugue por serialización o logs.
3. **Extensión al bus**: los mismos campos viajan cifrados dentro de Kafka (esquemas Avro con campos `bytes`; la transformación cifra antes de serializar) — [ADR-0029](../adr/adr-0029-pii-cifrada-end-to-end-kafka.md). BD cifrada + bus cifrado + logs sanitizados = triángulo cerrado.

## Trade-offs honestos

| A favor | En contra |
|---|---|
| PII opaca ante dumps/backups/acceso directo | Rotación de claves HMAC invalida índices (re-indexación necesaria) |
| Unicidad real sobre datos cifrados | Documentos más grandes (data + index por campo) |
| Sin KMS externo requerido | Solo igualdad exacta — sin LIKE/rangos sobre cifrados |

**Cuándo NO usarlo:** datos que requieren búsqueda parcial (búsqueda textual de nombres) — ahí se necesitan técnicas más pesadas (blind indexes con n-gramas, o arquitecturas dedicadas). Para unicidad y lookup exacto, este patrón es el punto dulce.

## Dónde verlo

- Orquestador y políticas: `jobby/shared/src/main/java/com/jobby/infrastructure/security/`
- Índices reales: `user-service/docker/sources/mongo/mongo-init-script.js`
- Cifrado en eventos: `CreatedUserEventToCreatedUserAvro.java` + `created-user-event.avsc`
- Checklist completo: [`docs/internal/guides/security-guide.md`](../../internal/guides/security-guide.md)

## Para ir más profundo

- NIST SP 800-38D — especificación GCM
- *Cryptographic Searchable Encryption* — literatura académica sobre blind indexes y sus límites
- [ADR-0030](../adr/adr-0030-sanitizacion-errores-support-id.md) — la tercera pata del triángulo: logs sin fugas
