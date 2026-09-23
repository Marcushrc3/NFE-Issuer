package com.example.nfe.api.controller;

import com.example.nfe.api.dto.ErrorResponse;
import com.example.nfe.api.dto.NfeEmissionRequest;
import com.example.nfe.api.dto.NfeEmissionResponse;
import com.example.nfe.application.service.NfeEmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nfe")
public class NfeController {

    private final NfeEmissionService nfeEmissionService;

    public NfeController(NfeEmissionService nfeEmissionService) {
        this.nfeEmissionService = nfeEmissionService;
    }

    @Operation(
            summary = "Issue an NF-e (NF-e 4.00 / PL_010f)",
            description = """
                    Generates the NF-e XML from the request, validates it against the
                    official bundled PL_010f XSD schemas and processes it according
                    to processingMode:

                    - XML_ONLY: returns the unsigned, XSD-validated infNFe payload.
                      No signing, no credentials and no SEFAZ contact.

                    - EMIT: signs the generated XML with the configured A1
                      certificate (PKCS#12), validates the exact signed document
                      against the complete official schema, transmits it to the
                      configured SEFAZ endpoint and maps the outcome to
                      AUTHORIZED / REJECTED / PROCESSING.

                    EMIT requires signing and SEFAZ configuration (see README);
                    without it the request fails with a controlled configuration
                    error. All fiscal values are supplied explicitly by the client —
                    the application never calculates, derives or defaults them.""")
    @ApiResponses({
            @ApiResponse(responseCode = "202",
                    description = "Request accepted and processed: status XML_GENERATED (XML_ONLY) "
                            + "or AUTHORIZED / REJECTED / PROCESSING (EMIT)."),
            @ApiResponse(responseCode = "400",
                    description = "Request rejected: bean validation, domain invariants or "
                            + "access-key check-digit (cDV) mismatch.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Configuration error (signing or SEFAZ unavailable), SEFAZ "
                            + "transmission failure, or generated-XML XSD validation failure.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<NfeEmissionResponse> issueNfe(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "NF-e emission request (fields optional in the DTO are "
                            + "mandatory for a schema-valid NF-e)",
                    content = @Content(examples = {
                            @ExampleObject(name = "XML_ONLY (complete valid request)",
                                    summary = "Generates and returns the unsigned, XSD-validated XML",
                                    value = XML_ONLY_EXAMPLE),
                            @ExampleObject(name = "EMIT (complete valid request)",
                                    summary = "Signs, validates and transmits to SEFAZ — "
                                            + "requires signing and SEFAZ configuration",
                                    value = EMIT_EXAMPLE)
                    }))
            @Valid @RequestBody NfeEmissionRequest request) {
        NfeEmissionResponse response = nfeEmissionService.issue(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    private static final String XML_ONLY_EXAMPLE = """
            {
              "externalReference": "REF-1",
              "operationType": "TRANSFER",
              "model": "55",
              "series": 3,
              "number": 1000,
              "emissionDate": "2026-09-21T10:30:00-03:00",
              "operationDescription": "Remessa para industrializacao",
              "fiscalEstablishmentCity": "3550308",
              "stateCode": "35",
              "randomCode": "00000001",
              "operationDirection": "1",
              "destinationType": "1",
              "printFormat": "1",
              "emissionType": "1",
              "checkDigit": "8",
              "environment": "2",
              "purpose": "1",
              "finalConsumer": "1",
              "presenceIndicator": "1",
              "processType": "0",
              "processVersion": "1.0",
              "issuer": {
                "name": "Acme Ltd",
                "document": "12345678000199",
                "tradeName": "Acme Comercio Ltda",
                "taxRegime": "3",
                "address": {
                  "street": "Main St", "number": "100", "city": "Sao Paulo", "state": "SP",
                  "zipCode": "01310100", "neighborhood": "Centro", "municipalityCode": "3550308"
                }
              },
              "recipient": {
                "name": "Beta Corp",
                "document": "98765432000188",
                "ieStatus": "CONTRIBUTOR",
                "address": {
                  "street": "Rua B", "number": "200", "city": "Sao Paulo", "state": "SP",
                  "zipCode": "01310200", "neighborhood": "Centro", "municipalityCode": "3550308"
                }
              },
              "items": [
                {
                  "productCode": "P-1",
                  "description": "Widget",
                  "ncm": "84818090",
                  "cEan": "7891234567895",
                  "cEanTrib": "7899876543210",
                  "unit": "UN",
                  "quantity": 1,
                  "unitValue": 10.50,
                  "totalValue": 10.50,
                  "tributaryUnit": "UN",
                  "tributaryQuantity": 1,
                  "tributaryUnitValue": 10.50,
                  "cfop": "5102",
                  "origin": "0",
                  "indTot": true,
                  "taxation": {
                    "icms": {
                      "cst": "00", "modBc": "3", "taxBase": 10.50, "taxRate": 18.00,
                      "taxAmount": 1.89, "fcpRate": 2.00, "fcpAmount": 0.21
                    }
                  }
                }
              ],
              "taxTotals": {
                "icms": {
                  "base": 10.50, "amount": 1.89, "desonerationAmount": 0.00, "fcp": 0.00,
                  "fcpUfDestination": 0.00, "ufDestination": 0.00, "ufSender": 0.00,
                  "stBase": 0.00, "stAmount": 0.00, "fcpStAmount": 0.00, "fcpStRetained": 0.00
                },
                "importTaxAmount": 0.00, "ipiAmount": 0.00, "ipiDevolvedAmount": 0.00,
                "pisAmount": 0.00, "cofinsAmount": 0.00, "totalTaxValue": 2.10
              },
              "payment": {
                "details": [
                  { "paymentMethod": "17", "amount": 10.50, "indicator": "IMMEDIATE", "paymentDate": "2026-09-21" }
                ]
              },
              "transport": {
                "freightMode": "0",
                "carrierDocument": "12345678000199",
                "carrierName": "Acme Carrier",
                "deliveryAddress": {
                  "street": "Port St", "number": "1", "city": "Santos", "state": "SP",
                  "zipCode": "11013000", "neighborhood": "Centro", "municipalityCode": "3548500"
                }
              },
              "processingMode": "XML_ONLY"
            }
            """;

    private static final String EMIT_EXAMPLE = """
            {
              "externalReference": "REF-1",
              "operationType": "TRANSFER",
              "model": "55",
              "series": 3,
              "number": 1000,
              "emissionDate": "2026-09-21T10:30:00-03:00",
              "operationDescription": "Remessa para industrializacao",
              "fiscalEstablishmentCity": "3550308",
              "stateCode": "35",
              "randomCode": "00000001",
              "operationDirection": "1",
              "destinationType": "1",
              "printFormat": "1",
              "emissionType": "1",
              "checkDigit": "8",
              "environment": "2",
              "purpose": "1",
              "finalConsumer": "1",
              "presenceIndicator": "1",
              "processType": "0",
              "processVersion": "1.0",
              "issuer": {
                "name": "Acme Ltd",
                "document": "12345678000199",
                "tradeName": "Acme Comercio Ltda",
                "taxRegime": "3",
                "address": {
                  "street": "Main St", "number": "100", "city": "Sao Paulo", "state": "SP",
                  "zipCode": "01310100", "neighborhood": "Centro", "municipalityCode": "3550308"
                }
              },
              "recipient": {
                "name": "Beta Corp",
                "document": "98765432000188",
                "ieStatus": "CONTRIBUTOR",
                "address": {
                  "street": "Rua B", "number": "200", "city": "Sao Paulo", "state": "SP",
                  "zipCode": "01310200", "neighborhood": "Centro", "municipalityCode": "3550308"
                }
              },
              "items": [
                {
                  "productCode": "P-1",
                  "description": "Widget",
                  "ncm": "84818090",
                  "cEan": "7891234567895",
                  "cEanTrib": "7899876543210",
                  "unit": "UN",
                  "quantity": 1,
                  "unitValue": 10.50,
                  "totalValue": 10.50,
                  "tributaryUnit": "UN",
                  "tributaryQuantity": 1,
                  "tributaryUnitValue": 10.50,
                  "cfop": "5102",
                  "origin": "0",
                  "indTot": true,
                  "taxation": {
                    "icms": {
                      "cst": "00", "modBc": "3", "taxBase": 10.50, "taxRate": 18.00,
                      "taxAmount": 1.89, "fcpRate": 2.00, "fcpAmount": 0.21
                    }
                  }
                }
              ],
              "taxTotals": {
                "icms": {
                  "base": 10.50, "amount": 1.89, "desonerationAmount": 0.00, "fcp": 0.00,
                  "fcpUfDestination": 0.00, "ufDestination": 0.00, "ufSender": 0.00,
                  "stBase": 0.00, "stAmount": 0.00, "fcpStAmount": 0.00, "fcpStRetained": 0.00
                },
                "importTaxAmount": 0.00, "ipiAmount": 0.00, "ipiDevolvedAmount": 0.00,
                "pisAmount": 0.00, "cofinsAmount": 0.00, "totalTaxValue": 2.10
              },
              "payment": {
                "details": [
                  { "paymentMethod": "17", "amount": 10.50, "indicator": "IMMEDIATE", "paymentDate": "2026-09-21" }
                ]
              },
              "transport": {
                "freightMode": "0",
                "carrierDocument": "12345678000199",
                "carrierName": "Acme Carrier",
                "deliveryAddress": {
                  "street": "Port St", "number": "1", "city": "Santos", "state": "SP",
                  "zipCode": "11013000", "neighborhood": "Centro", "municipalityCode": "3548500"
                }
              },
              "processingMode": "EMIT"
            }
            """;
}
