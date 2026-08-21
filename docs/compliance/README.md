# Cumplimiento Normativo Colombiano

* Estado: 🚧 en construcción — este módulo documental se desarrollará junto con `billing-service` (Fase 2 del [roadmap](../project/roadmap.md))

---

Este directorio documentará el cumplimiento de Jobby ERP con el marco regulatorio de la facturación electrónica en Colombia:

| Tema | Marco normativo | Estado doc |
|---|---|---|
| Requisitos técnicos de FE | Resolución DIAN 000042 de 2020 (y modificatorias) | Pendiente |
| Estándar de documentos | UBL 2.1 (DIAN) | Pendiente |
| Códigos únicos | CUFE (factura) / CUDE (documentos equivalentes) | Pendiente |
| Firma digital | XAdES-B con certificado emitido por DIAN | Pendiente |
| Contingencia | Emisión offline + sincronización posterior | Pendiente |
| Validación previa | Habilitación y operación en producción DIAN | Pendiente |

## Referencias oficiales

- Portal de facturación electrónica de la DIAN: https://www.dian.gov.co
- Catálogo UBL y anexos técnicos publicados por la DIAN

## Convención

Cada tema tendrá su documento propio con: requisitos normativos citados, cómo los implementa el código (con links a ADRs y clases), y evidencia de validación. Mientras tanto, el glosario de términos fiscales vive en [`docs/project/glossary.md`](../project/glossary.md).
