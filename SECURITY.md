# Política de Seguridad / Security Policy

## Índice / Index
- [Español](#español)
    - [Versiones soportadas](#versiones-soportadas)
    - [Reportar una vulnerabilidad](#reportar-una-vulnerabilidad)
    - [Medidas de seguridad](#medidas-de-seguridad)
- [English](#english)
    - [Supported versions](#supported-versions)
    - [Reporting a vulnerability](#reporting-a-vulnerability)
    - [Security measures](#security-measures)

---

## Español

### Versiones soportadas
Solo la **última versión estable** recibe parches de seguridad.

### Reportar una vulnerabilidad
**No uses issues públicos.** Envía un correo a **heyther.dev@gmail.com** incluyendo:
- Tipo de problema
- Componente afectado
- Pasos para reproducir
- Impacto potencial

Recibirás respuesta en **48 horas** y el problema será validado en **5 días hábiles**.

### Medidas de seguridad
- API Gateway con rate limiting
- Base de datos por servicio (aislamiento)
- Firmas XAdES-B para facturación electrónica
- Escaneo de dependencias (Dependabot)
- SAST en CI (CodeQL)

---

## English

### Supported versions
Only the **latest stable release** receives security patches.

### Reporting a vulnerability
**Do not use public issues.** Send an email to **heyther.dev@gmail.com** including:
- Issue type
- Affected component
- Reproduction steps
- Potential impact

You will receive a response within **48 hours** and validation within **5 business days**.

### Security measures
- API Gateway with rate limiting
- Database per service (isolation)
- XAdES-B signatures for electronic invoicing
- Dependency scanning (Dependabot)
- SAST in CI (CodeQL)

---

*Gracias por ayudar a mantener Jobby ERP seguro / Thank you for helping keep Jobby ERP secure.*