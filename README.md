# NF-e Service

## Overview

REST service that issues Brazilian electronic invoices (NF-e, layout 4.00 / PL_010f).

The application receives a strongly-typed JSON request, builds the NF-e domain aggregate, generates the XML (JAXB) in the exact official schema order, validates it against the **official PL_010f XSDs bundled in the classpath**, and either returns the unsigned XML or signs it (XMLDSig with an A1 PKCS#12 certificate) and transmits it to the configured SEFAZ endpoint.

The service is fully independent from any legacy system: the only entry point is the HTTP API, and future integrations can plug in through adapters around the same application/domain layers.

## Current Status

**Implemented**

- `POST /api/v1/nfe` — NF-e emission endpoint (details in [API](#api) and Swagger UI).
- `processingMode = XML_ONLY`: generates and XSD-validates the unsigned `infNFe`; returns the XML. No credentials or SEFAZ contact.
- `processingMode = EMIT`: signs the exact generated XML, validates the signed document against the complete official schema, transmits it to SEFAZ and maps the outcome to `AUTHORIZED` / `REJECTED` / `PROCESSING`. Never fabricates an authorization result.
- Access key (44-digit `chave de acesso`) with official modulo-11 check digit (`cDV`); explicit `cDV` mismatch is rejected.
- Full API surface for a schema-valid NF-e request: `ide` identification fields, issuer/recipient addresses, product tributary fields (`cEAN`, `cEANTrib`, `uTrib/qTrib/vUnTrib`, `indTot`), item taxation (ICMS 00/30/40, IPI, PIS, COFINS, II, IS, IBSCBS/RTC), `taxTotals`/`ICMSTot`, payment and transport (`modFrete` + carrier).
- XMLDSig signing (RSA-SHA1 / SHA-1, official NF-e parameters) and hardened XML parsing (XXE protection, DOCTYPE rejected).
- SEFAZ NFeAutorizacao4 transmission adapter (opt-in) with structured result mapping; integration tests use an in-process SEFAZ mock.
- Standardized `ErrorResponse` body for all 4xx/5xx failures.

**Partially implemented / not yet exposed by the API**

- `ItemDto`: `cest`, `xPed`, `nItemPed` and per-item import details exist in the domain but are not exposed yet.
- `TaxTotals.is` and `TaxTotals.ibsCbs` (the `total/ISTot` and `total/IBSCBSTot` XML groups) are accepted by the API but not mapped to XML yet.
- PIS/COFINS/IPI official variant groups (`PISAliq`, `PISQtde`, `PISST`, `IPITrib`, `IPINT`, …) are deliberately unsupported by the current mapper.
- `ICMSTot` monophase fields and several optional `transp` groups (`retTransp`, vehicles, volumes) are not modeled.

**Not implemented**

- Persistence/storage of emissions or protocols.
- DANFE generation.
- Contingency emission modes (FS/FSDA/SVC, …).
- Batch or asynchronous processing; retries.

## Architecture

```
Client (REST JSON)
   ↓
API layer          controllers + DTOs + Bean Validation + ErrorResponse
   ↓
Application layer  NfeEmissionService (XML_ONLY/EMIT orchestration),
                   access-key generation, SEFAZ transmission orchestration
   ↓
Domain layer       NfeEmission aggregate and value objects (no XML/JAXB concepts,
                   no fiscal calculations — every value is explicit)
   ↓
XML/XSD layer      NfeXmlMapper + JAXB model (exact PL_010f element order)
                   + NfeXmlValidator (bundled official XSDs, classpath-only)
   ↓
Signature layer    XmlDsigSigner (XMLDSig, A1 PKCS#12 credentials)
   ↓
SEFAZ adapter      SefazTransmitter → NFeAutorizacao4 WebService
```

Future integrations enter through the same application/domain contracts via adapters — the domain does not know about HTTP, XML or SEFAZ.

## Tech Stack

- Java 21
- Spring Boot 3.5.0 (Web, Validation)
- Maven 3.9+ (no Maven wrapper in the repository)
- JAXB (`jakarta.xml.bind-api` + Glassfish `jaxb-runtime`)
- JDK XMLDSig (RSA-SHA1 / SHA-1, official NF-e signing parameters)
- springdoc-openapi 2.8.6 (Swagger UI / OpenAPI)
- JUnit 5 + Mockito + Spring Test (tests)

## Requirements

- JDK 21
- Maven 3.9+ on `PATH` (the project has no `mvnw` wrapper)
- Internet access for the first Maven dependency download
- For `EMIT`: an A1 certificate in PKCS#12 format and SEFAZ network access (see [Configuration](#configuration))

## Running locally

```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080` (default Spring Boot port — no `server.port` override).

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

### Configuration

`src/main/resources/application.yml`:

| Property | Default | Purpose |
|---|---|---|
| `nfe.signing.enabled` | `false` (`NFE_SIGNING_ENABLED`) | Enables XMLDSig signing for `EMIT` |
| `nfe.signing.keystore-path` | empty (`NFE_SIGNING_KEYSTORE_PATH`) | PKCS#12 A1 certificate path |
| `nfe.signing.keystore-password` | empty (`NFE_SIGNING_KEYSTORE_PASSWORD`) | Keystore password |
| `nfe.signing.alias` | empty (`NFE_SIGNING_ALIAS`) | Key alias |
| `nfe.sefaz.enabled` | `false` | Enables real SEFAZ transmission for `EMIT` |
| `nfe.sefaz.endpoint` | SEFAZ-SP homologation | NFeAutorizacao4 WebService URL |
| `nfe.sefaz.uf` / `nfe.sefaz.environment` | `SP` / `HOMOLOGATION` | Target SEFAZ environment |

Secrets are supplied through environment variables at runtime and are never committed to source control.

## API

### `POST /api/v1/nfe` — issue an NF-e

Request and response examples, field documentation, enum values and status codes are available in Swagger UI (`http://localhost:8080/swagger-ui/index.html`).

| `processingMode` | Behaviour |
|---|---|
| `XML_ONLY` | Generate → validate unsigned `infNFe` against official XSD → return `202` with `status = XML_GENERATED` and the unsigned XML. |
| `EMIT` | Generate → sign → validate the signed document → transmit to SEFAZ → return `202` with `status = AUTHORIZED`, `REJECTED` or `PROCESSING`. Requires signing + SEFAZ configuration. |

Response statuses (always with a JSON body):

| HTTP | Meaning |
|---|---|
| `202` | Processed — see `status` field (`XML_GENERATED`, `AUTHORIZED`, `REJECTED`, `PROCESSING`) |
| `400` | Invalid request: bean validation, domain invariants, or access-key `cDV` mismatch |
| `500` | Signing/SEFAZ configuration error, SEFAZ transmission failure, or generated-XML XSD validation failure |

Enums currently accepted: `operationType` (`IMPORT`, `TRANSFER`, `EXPORT`), `processingMode` (`XML_ONLY`, `EMIT`), `ieStatus` (`CONTRIBUTOR`, `EXEMPT`, `NOT_CONTRIBUTOR`), `indicator` (`IMMEDIATE`, `DEFERRED`). Freight mode and tax codes are plain strings validated by the official XSD enumeration/patterns.

### EMIT prerequisites

`EMIT` does **not** fabricate anything: signing credentials and a SEFAZ transmitter must be configured, otherwise the request fails with a controlled configuration error (`500`).

## Testing

```bash
mvn test
```

Notable test suites:

- `NfeEmissionServiceTest` — orchestration and API→domain mapping.
- `NfeEmissionServiceRealPipelineTest` — real generator + validator + signer wired into the service (SEFAZ mocked).
- `NfeXmlGeneratorTest` / `NfeXmlValidatorTest` — XML generation order and official XSD validation.
- `NfeAccessKeyGeneratorTest` — official modulo-11 check digit.
- `NfeEmissionFlowIntegrationTest` — full HTTP end-to-end flow against an in-process SEFAZ mock (no real SEFAZ contact, no production certificates).

> Current known state: the HTTP end-to-end scenarios in `NfeEmissionFlowIntegrationTest` that assert `202` still fail because their fixture does not yet use the full request surface added in increments 50–54 (the fixture update is tracked separately). The focused suites above are green.
