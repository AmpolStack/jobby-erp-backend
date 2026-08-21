# ADR-0035: Códigos efímeros en Redis con TTL y consumo único

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

Los flujos sensibles de identidad (cambio de email, y futuros: recuperación de contraseña, verificación de dispositivos) requieren confirmación mediante código de un solo uso con expiración corta. El mecanismo debe garantizar: expiración automática, consumo único (un código no sirve dos veces), y resistencia a fuerza bruta.

## Impulsores de la decisión

* Expiración semántica nativa (TTL) sin jobs de limpieza.
* Consumo atómico: validar y eliminar en una operación.
* Almacenamiento compartible entre réplicas del servicio.
* Generación criptográficamente segura del código.

## Opciones consideradas

### Opción A: Columna/colección en MongoDB con campo `expiresAt`

* Bien, porque un solo storage.
* Malo, porque limpieza manual (TTL index aparte), consumo único requiere transacción update-condicional, y mezcla datos efímeros con persistencia de negocio.

### Opción B: JWT de un solo uso firmado

* Bien, porque stateless.
* Malo, porque revocación/consumo único exige estado igualmente; el token expone payload decodificable; overkill para un código de 8 caracteres.

### Opción C: Clave Redis con TTL + eliminación al validar

`EmailChangeCodeServiceAdapter`: código de 8 caracteres alfanuméricos generado con `SecureRandom`; guarda `{code, newEmail}` en Redis bajo clave `user.change-email.{userId}` con TTL de 5 minutos; `validate()` compara **y elimina la clave** — consumo único garantizado. Record privado `ChangeCodeRequest` como value serializado.

* Bien, porque TTL nativo; consumo único atómico por diseño (get+delete sobre clave única); compartido entre réplicas; cero limpieza.
* Malo, porque dependencia de Redis para flujos críticos de identidad (mitigado: fallo de Redis = flujo degradado con error explícito, nunca inseguro).

## Decisión

Opción elegida: **C**. Reglas generalizables a todo código efímero:

1. Clave namespaced por usuario (`user.<flujo>.{userId}`).
2. Código generado con `SecureRandom`, longitud ≥ 8.
3. TTL ≤ 5 minutos para flujos de identidad.
4. Validación exitosa **elimina** la clave — reuso imposible.
5. El value transporta el contexto mínimo necesario para completar el flujo (ej. nuevo email).

### Justificación

Redis es exactamente la herramienta para datos con vida corta y semántica de expiración. El patrón "clave única + TTL + delete-on-validate" resuelve los tres requisitos (expiración, unicidad de uso, anti-replay) con las primitivas nativas del storage, sin jobs ni transacciones.

## Consecuencias

* **Positivas**: flujos de identidad seguros por construcción; cero mantenimiento de datos efímeros; métricas `redis.*` ya instrumentadas ([ADR-0013](adr-0013-observabilidad-self-hosted.md)).
* **Negativas**: acoplamiento operativo a Redis para estos flujos (aceptado explícitamente).
* **Neutras**: el patrón es replicable para futuros flujos (recuperación de contraseña, OTP POS).

## Validación

Tests unitarios del adapter (generación, validación, consumo único, expiración simulada); observaciones `redis.register/get/remove` visibles.

## Referencias

* [ADR-0012](adr-0012-redis-cache-factory-propia.md)
