# Estrategia de Contenido — LinkedIn & Medium

* Tipo: estrategia editorial · Última actualización: 2026-08-20
* Naturaleza: documento vivo — se ajusta con métricas reales cada mes

---

## Objetivo del contenido

1. **Construir audiencia para el proyecto**: desarrolladores Java/backend LATAM interesados en microservicios reales, no tutoriales.
2. **Alimentar el objetivo educativo del proyecto**: cada pieza de contenido nace de documentación que ya existe ([patrones](../architecture/patterns/README.md), [ADRs](../architecture/adr/README.md)) — nunca se inventa contenido paralelo.
3. **Build in public honesto**: mostrar también errores, refactors y deudas — es diferenciador y coherente con los principios del proyecto.

## Pilares temáticos (6)

| Pilar | Qué cubre | Fuente principal | Peso |
|---|---|---|---|
| **Arquitectura** | Hexagonal, DDD, CQRS, monorepo vs polyrepo | patterns/hexagonal-ddd, cqrs-buses + ADRs grupo A/C | 25% |
| **Patrones aplicados** | Result, eventos de dominio, outbox, transacciones explícitas | patterns/* + ADRs 0016-0025 | 25% |
| **Seguridad** | Searchable encryption, PII en Kafka, sanitización | patterns/searchable-encryption + ADRs grupo D | 15% |
| **Infraestructura** | Kafka KRaft, Apicurio, observabilidad self-hosted, Docker Compose | ADRs grupo B + messaging-guide | 15% |
| **Dominio colombiano** | DIAN, UBL, CUFE/CUDE, contingencia — el nicho que casi nadie cubre | compliance/ + glossary | 10% |
| **Build in public** | Retrospectivas, deudas, decisiones descartadas, métricas del proyecto | backlog + ADRs "descartadas" | 10% |

## Formato por plataforma

### LinkedIn — 2 posts/semana (martes y viernes)

Micro-contenido: una idea por post. Formatos rotativos:

| Formato | Estructura | Ejemplo |
|---|---|---|
| **Decisión** | Problema → opciones → decisión → trade-off | "¿CQRS completo o parcial? Elegimos el 80/20" |
| **Snippet** | Código real (≤15 líneas) + explicación de 3 líneas | La fábrica `of()`/`reconstruct()` |
| **Aprendizaje** | Error/lección del build in public | "Por qué descartamos AOP para eventos" |
| **Pregunta** | Dilema arquitectónico abierto a la comunidad | "¿Rotación de claves HMAC: cómo lo hacen?" |

Reglas: español, hook en la primera línea, sin hashtags excesivos (3-4), siempre link al repo cuando aplique.

### Medium — 1 artículo/quincena (jueves)

Deep dive de 6-10 min derivado de una guía de patrón. Estructura fija:

1. El problema (dolor concreto, no teoría)
2. El patrón con código real del repo
3. Trade-offs honestos (incluyendo cuándo NO usarlo)
4. Lo que aprendimos construyéndolo
5. Link al repo + al ADR correspondiente

Los artículos se republican/adaptan: versión corta en LinkedIn apuntando a Medium.

## Calendario — 12 semanas

> Semana 1 = lanzamiento público del repo. Los temas siguen el orden de implementación real del proyecto; si el desarrollo se adelanta/atrasa, se intercambian semanas completas (no se mezclan pilares).

| Semana | Martes (LinkedIn) | Viernes (LinkedIn) | Jueves alterno (Medium) |
|---|---|---|---|
| **1** | 🚀 Lanzamiento: qué es Jobby ERP y por qué open source | El problema: por qué los sistemas de facturación se caen | Art. 1: Presentando Jobby ERP |
| **2** | Dominio puro: la regla del grep (cero imports Spring) | Snippet: un Value Object autovalidado | — |
| **3** | Result Pattern: adiós a las excepciones de negocio | Snippet: pipeline flatMap de un caso de uso real | Art. 2: Hexagonal + DDD sin pedir permiso |
| **4** | ValidationChain: validaciones como datos, no como ifs | Build in public: las deudas que registramos antes de crecer | — |
| **5** | CQRS parcial: la regla del 80/20 (8 commands vs 1 query) | Snippet: buses con registro explícito, cero reflexión | Art. 3: Result Pattern en dominio fiscal |
| **6** | Explicitness over magic: el principio que decide todo | Pregunta: ¿qué automatismos del framework aceptas? | — |
| **7** | Domain Events: el agregado declara hechos, el mundo reacciona | Por qué descartamos AOP para ordeñar eventos | Art. 4: CQRS sin sobre-ingeniería |
| **8** | reconstruct() jamás emite eventos: el bug que evitamos | Snippet: pullDomainEvents() post-transacción | — |
| **9** | 🔐 Buscar en datos cifrados: la paradoja del email único | Snippet: índice HMAC + UNIQUE constraint en Mongo | Art. 5: Eventos de dominio que no mienten |
| **10** | PII cifrada hasta en Kafka: bytes en el .avsc | El triángulo: BD cifrada + bus cifrado + logs sanitizados | — |
| **11** | Kafka 4.0 KRaft + Apicurio hablando Confluent | Observabilidad self-hosted: una envoltura, dos señales | Art. 6: Searchable encryption sin KMS |
| **12** | Outbox Pattern: cuándo sí, cuándo todavía no | 📊 Retrospectiva 12 semanas: métricas, aprendizajes, siguiente fase | Art. 7: Apicurio + wire format Confluent |

## Plantillas

### Post LinkedIn — formato Decisión

```
[Hook: la tensión en 1 línea]

Contexto: 2-3 líneas del problema real del proyecto.

Las opciones:
→ Opción A: consecuencia
→ Opción B: consecuencia

Elegimos X porque [razón en 1 línea].

El trade-off que aceptamos: [honestidad].

[Link al ADR/repo]
#java #microservicios #arquitectura
```

### Artículo Medium — checklist de publicación

- [ ] Derivado de una guía de patrón existente (no escrito desde cero)
- [ ] Código real del repo verificado que compila/existe
- [ ] Sección "cuándo NO usarlo" incluida
- [ ] Link al repo + ADR al final
- [ ] Versión corta programada en LinkedIn

## Métricas y revisión

- **Revisión mensual**: alcance/engagement por pilar → ajustar pesos.
- **Señal de éxito primaria**: estrellas/forks/issues del repo atribuibles a contenido (preguntar en issues "¿cómo llegaste?").
- **Señal secundaria**: seguidores que comentan con preguntas técnicas profundas.

## Reglas transversales

1. Nunca publicar contenido de una feature que no está en el repo (el código manda).
2. Cada pieza enlaza al repo — el contenido sirve al proyecto, no viceversa.
3. Las deudas y errores son contenido válido (pilar build in public) — nunca ocultarlos.
4. Idioma: español primero. Evaluación de traducción al inglés tras la semana 12 según métricas.
