# ADR-0026: Strategy con autodescubrimiento Spring

* Fecha: 2026-08-20
* Estado: aceptada

## Contexto y problema

El caso de uso de actualización de imagen de perfil tenía un `switch(user.getRole())` que decidía cómo resolver el contexto de almacenamiento según el rol (Owner vs Employee). Cada nuevo rol exigía modificar el switch — violación clásica de OCP en un punto que claramente crecería (roles futuros, variantes por plan).

El análisis (`strategy-pattern`, hoy absorbido aquí) formalizó además *cuándo* conviene Strategy y cuándo es sobre-ingeniería.

## Impulsores de la decisión

* OCP: agregar una variante no debe tocar el código existente.
* Variantes con lógica propia y dependencias distintas.
* Registro automático: nuevas implementaciones sin tocar configuración central.

## Opciones consideradas

### Opción A: Mantener el switch

* Bien, porque simple y visible para 2 variantes.
* Malo, porque cada variante nueva modifica código probado; las ramas crecen y se vuelven métodos gigantes; testing del switch = testing de todo.

### Opción B: Map estático de estrategias registrado manualmente

* Bien, porque despacho O(1) explícito.
* Malo, porque registro manual que olvidar al agregar variantes; duplica lo que Spring ya sabe hacer.

### Opción C: Strategy con autodescubrimiento por inyección de List

Interfaz con método de discriminación + implementaciones como `@Component`; el consumidor inyecta `List<Interfaz>` y construye `Map<Role, Resolver>` en su constructor:

```java
public interface ProfileImageContextResolver {
    Role supportedRole();
    Result<ImageStorageContext, Error> resolve(...);
}

// en el use case
public UpdateProfileImageUseCaseAdapter(List<ProfileImageContextResolver> resolvers, ...) {
    this.resolvers = resolvers.stream()
        .collect(toMap(ProfileImageContextResolver::supportedRole, r -> r));
}
// rol sin handler → Result.failure("No handler for role: X") explícito
```

* Bien, porque agregar variante = crear una clase `@Component` y nada más; fallo explícito si falta handler; testable inyectando lista fake.
* Malo, porque patrón menos obvio para newcomers (documentado); riesgo de Map mutable si no se construye en constructor.

## Decisión

Opción elegida: **C**. Criterio de aplicación documentado:

- **Usar Strategy** con 3+ variantes, cambios por razones distintas, lógica compleja o variantes futuras esperadas.
- **Mantener switch** con 2 variantes estables y ramas cortas (5-10 líneas).

Candidatos futuros identificados con este patrón: validador de identificación por tipo de documento colombiano (CC/NIT/PPT), proveedor de storage (S3/local vía `@ConditionalOnProperty`), canal de notificación (email/SMS), calculadora de nómina por tipo de contrato, exportador de reportes (PDF/Excel/CSV).

### Justificación

Es un meta-patrón recurrente en el codebase (buses, handlers de eventos, resolvers): interfaz con discriminación + inyección de List + Map + failure explícito. Estandarizarlo evita que cada equipo lo reinvente distinto.

## Consecuencias

* **Positivas**: OCP real; registro automático; fallo explícito ante rol sin handler; tests unitarios sin Spring.
* **Negativas**: indirección adicional al leer el flujo (mitigada por convención de naming `*Resolver`).
* **Neutras**: complementa —no reemplaza— las autoconfiguraciones condicionales ([ADR-0005](adr-0005-shared-como-starter-autoconfiguracion.md)).

## Validación

Review verifica el criterio 2-vs-3 variantes antes de aceptar refactors a Strategy; el caso ProfileImageContextResolver sirve de ejemplo canónico.

## Referencias

* [ADR-0020](adr-0020-buses-in-process-command-query-event.md) — mismo meta-patrón en buses
