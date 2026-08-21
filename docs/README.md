# Documentación — Jobby ERP

Bienvenido al sistema documental de **Jobby ERP**. Este directorio es parte central del proyecto: una de nuestras metas explícitas es que cualquier desarrollador pueda estudiar este repositorio y salir con una comprensión práctica y profunda de los sistemas distribuidos aplicados a un producto real.

Toda la documentación está escrita en **español**.

---

## Mapa de la documentación

```
docs/
├── project/                  → Formalización y definición del proyecto
├── architecture/
│   ├── adr/                  → Architecture Decision Records (formato MADR)
│   └── patterns/             → Guías educativas de patrones (públicas)
├── internal/
│   └── guides/               → Documentación técnica interna (how-to de desarrollo)
├── articles/                 → Serie de artículos técnicos + estrategia de contenido
├── compliance/               → Normativa DIAN y cumplimiento colombiano
├── guides/                   → Guías de uso (setup local, etc.)
└── assets/                   → Logo y diagramas
```

---

## ¿Por dónde empezar?

Depende de quién eres:

| Perfil | Ruta recomendada |
|---|---|
| **Quiero entender el proyecto** | [`project/vision.md`](project/vision.md) → [`project/scope.md`](project/scope.md) |
| **Quiero aprender la arquitectura** | [`architecture/patterns/`](architecture/patterns/) → ADRs enlazados desde cada patrón |
| **Quiero contribuir código** | [`internal/guides/architecture-rules.md`](internal/guides/architecture-rules.md) → [`project/principles.md`](project/principles.md) |
| **Quiero montar el entorno** | [`guides/local-setup.md`](guides/local-setup.md) |
| **Me interesa el dominio DIAN** | [`compliance/README.md`](compliance/README.md) → [`project/glossary.md`](project/glossary.md) |
| **Sigo la serie de artículos** | [`articles/README.md`](articles/README.md) |

---

## Las tres capas documentales

El sistema documental se organiza en tres capas con propósitos distintos:

### 1. Decisiones — `architecture/adr/`

Cada decisión arquitectónica significativa se registra como un **ADR** en formato [MADR](https://adr.github.io/madr/) 3.0.0: contexto, opciones consideradas, decisión y consecuencias. Los ADRs son inmutables en la práctica: cambiar una decisión genera un nuevo ADR que sustituye al anterior. Ver [`architecture/adr/README.md`](architecture/adr/README.md).

### 2. Patrones (público) — `architecture/patterns/`

Guías narrativas y educativas que explican cada patrón aplicado en el contexto real de este codebase: cómo funciona, por qué lo elegimos, sus trade-offs y cómo se manifiesta en el código. Son el material base de la serie de artículos. Complementan a los ADRs: el ADR registra *qué se decidió*, la guía explica *cómo funciona y por qué aprenderlo*.

### 3. Guías internas — `internal/guides/`

Documentación operativa para quienes desarrollan el proyecto día a día: reglas de arquitectura verificables, convenciones de testing, guía de eventos de dominio, seguridad, mensajería y observabilidad. Aquí vive el *how-to*; las decisiones subyacentes viven en los ADRs.

---

## Convenciones

- **Idioma**: español para toda la documentación; inglés para código.
- **ADRs**: numeración secuencial `NNNN`, un archivo por decisión, estados `Propuesta` / `Aceptada` / `Sustituida`.
- **Referencias cruzadas**: los ADRs enlazan a guías internas y patrones públicos; las guías nunca duplican una decisión, la referencian.
- **Código como fuente de verdad**: cuando un documento y el código discrepan, manda el código — y el documento se corrige o se abre issue.

---

## Mantenimiento

- Nueva decisión significativa → nuevo ADR (ver convenciones en [`architecture/adr/README.md`](architecture/adr/README.md)).
- Nueva convención de desarrollo → guía interna correspondiente + skill de OpenCode si aplica ([`internal/guides/opencode-skills.md`](internal/guides/opencode-skills.md)).
- Deudas técnicas detectadas → [`internal/backlog.md`](internal/backlog.md).
