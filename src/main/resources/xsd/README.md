# Official NF-e XSD schemas (bundled)

- **Schema package:** Pacote de Liberação nº 010f (PL_010f) — leiaute NF-e/NFC-e v4.00
- **Notas Técnicas:** NT 2025.002 v.1.50 e NT 2026.007 v.1.00
- **Published:** 31/08/2026
- **Obtained:** 2026-09-21
- **Source:** Portal Nacional da NF-e (official)
  - https://www.nfe.fazenda.gov.br/portal — Documentos → Esquemas XML
  - "Schemas XML NF-e - Pacote de Liberação nº 010f - NT 2025.002 v.1.50 e NT 2026.007 v.1.00"
- **Purpose:** authoritative source for validating generated NF-e XML
  (`NfeXmlValidator`). These files must NOT be modified.

## Files and dependency structure

| File | Role |
|---|---|
| `nfe_v4.00.xsd` | Entry point — declares the global `NFe` element (`TNFe`) |
| `leiauteNFe_v4.00.xsd` | Main invoice layout (`TNFe`) |
| `tiposBasico_v4.00.xsd` | Basic shared types |
| `DFeTiposBasicos_v1.00.xsd` | RTC/IBS/CBS types (`TTribNFe`, `TCIBS_NFe`, `TIS`, …) |
| `xmldsig-core-schema_v1.01.xsd` | XML signature schema (ICP-Brasil profile) |

Includes/imports (relative, resolved from this directory at runtime via the
classpath resource resolver):

```
nfe_v4.00.xsd
  └── xs:include → leiauteNFe_v4.00.xsd
         ├── xs:import → xmldsig-core-schema_v1.01.xsd
         │                (namespace http://www.w3.org/2000/09/xmldsig#)
         ├── xs:include → tiposBasico_v4.00.xsd
         └── xs:include → DFeTiposBasicos_v1.00.xsd
```

These schemas are used by `NfeXmlValidator` for validation. The domain model
and the current JAXB XML generation are incremental and are NOT yet fully
schema-complete — that is expected and tracked separately.
