package com.example.nfe.api.controller;

import com.example.nfe.api.dto.NfeEmissionResponse;
import com.example.nfe.api.dto.SefazAuthorizationResponseDto;
import com.example.nfe.api.dto.SefazTransmissionResponseDto;
import com.example.nfe.application.sefaz.SefazAuthorizationStatus;
import com.example.nfe.application.sefaz.SefazTransmissionStatus;
import com.example.nfe.application.service.NfeEmissionService;
import com.example.nfe.application.service.ProcessingMode;
import com.example.nfe.application.service.ProcessingStatus;
import com.example.nfe.application.service.SigningNotConfiguredException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NfeController.class)
class NfeControllerTest {

    private static final String XML_GENERATED_MESSAGE = "NF-e XML generated and XSD-validated";

    private static final String IMPORT_SECTION = """
            ,
              "importDetails": {
                "declarationNumber": "DI-123",
                "declarationDate": "2026-01-15",
                "clearancePlace": "Porto de Santos"
              }""";

    private static final String EXPORT_SECTION = """
            ,
              "exportDetails": {
                "destinationCountry": "United States"
              }""";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NfeEmissionService nfeEmissionService;

    @Test
    void shouldAcceptImportRequestWithImportDetails() throws Exception {
        stubReceivedResponse();

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest("IMPORT", IMPORT_SECTION)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.emissionId").value("emission-1"))
                .andExpect(jsonPath("$.status").value("XML_GENERATED"))
                .andExpect(jsonPath("$.message").value(XML_GENERATED_MESSAGE));
    }

    @Test
    void shouldRejectImportRequestWithoutImportDetails() throws Exception {
        when(nfeEmissionService.issue(any()))
                .thenThrow(new IllegalArgumentException("importDetails is required for IMPORT"));

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest("IMPORT", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("importDetails is required for IMPORT"));
    }

    @Test
    void shouldRejectImportRequestWithExportDetails() throws Exception {
        when(nfeEmissionService.issue(any()))
                .thenThrow(new IllegalArgumentException("exportDetails must be absent for IMPORT"));

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest("IMPORT", EXPORT_SECTION)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("exportDetails must be absent for IMPORT"));
    }

    @Test
    void shouldAcceptExportRequestWithExportDetails() throws Exception {
        stubReceivedResponse();

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest("EXPORT", EXPORT_SECTION)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.emissionId").value("emission-1"))
                .andExpect(jsonPath("$.status").value("XML_GENERATED"));
    }

    @Test
    void shouldRejectExportRequestWithoutExportDetails() throws Exception {
        when(nfeEmissionService.issue(any()))
                .thenThrow(new IllegalArgumentException("exportDetails is required for EXPORT"));

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest("EXPORT", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("exportDetails is required for EXPORT"));
    }

    @Test
    void shouldRejectExportRequestWithImportDetails() throws Exception {
        when(nfeEmissionService.issue(any()))
                .thenThrow(new IllegalArgumentException("importDetails must be absent for EXPORT"));

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest("EXPORT", IMPORT_SECTION)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("importDetails must be absent for EXPORT"));
    }

    @Test
    void shouldAcceptTransferRequestWithoutOperationSpecificDetails() throws Exception {
        stubReceivedResponse();

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest("TRANSFER", "")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.emissionId").value("emission-1"))
                .andExpect(jsonPath("$.status").value("XML_GENERATED"));
    }

    @Test
    void shouldAcceptRequestWithHeaderFields() throws Exception {
        stubReceivedResponse();

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestWithHeader("TRANSFER", "")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.emissionId").value("emission-1"))
                .andExpect(jsonPath("$.status").value("XML_GENERATED"));
    }

    @Test
    void shouldAcceptRequestWithIdentificationFields() throws Exception {
        stubReceivedResponse();

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestWithIdentification("TRANSFER", "")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.emissionId").value("emission-1"))
                .andExpect(jsonPath("$.status").value("XML_GENERATED"));
    }

    @Test
    void shouldAcceptRequestWithIssuerTradeNameAndTaxRegime() throws Exception {
        stubReceivedResponse();

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestWithIssuerIdentity("TRANSFER", "")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.emissionId").value("emission-1"))
                .andExpect(jsonPath("$.status").value("XML_GENERATED"));
    }

    @Test
    void shouldAcceptRequestWithRecipientIeStatus() throws Exception {
        stubReceivedResponse();

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestWithRecipientIeStatus("TRANSFER", "")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.emissionId").value("emission-1"))
                .andExpect(jsonPath("$.status").value("XML_GENERATED"));
    }

    @Test
    void shouldAcceptRequestWithPayment() throws Exception {
        stubReceivedResponse();

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestWithPayment("TRANSFER", "")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.emissionId").value("emission-1"))
                .andExpect(jsonPath("$.status").value("XML_GENERATED"));
    }

    @Test
    void shouldAcceptEmitRequestAndReturnSignedXml() throws Exception {
        when(nfeEmissionService.issue(any()))
                .thenReturn(new NfeEmissionResponse("emission-1",
                        ProcessingMode.EMIT, ProcessingStatus.READY_FOR_EMISSION,
                        "NF-e signed and ready for emission", "<signed/>", null));

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestWithProcessingMode("EMIT", "")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.processingMode").value("EMIT"))
                .andExpect(jsonPath("$.status").value("READY_FOR_EMISSION"))
                .andExpect(jsonPath("$.xml").value("<signed/>"));
    }

    @Test
    void shouldReturnAuthorizedEmitResponseWithTransmissionDetails() throws Exception {
        when(nfeEmissionService.issue(any()))
                .thenReturn(new NfeEmissionResponse("emission-1",
                        ProcessingMode.EMIT, ProcessingStatus.AUTHORIZED,
                        "NF-e signed and submitted to the SEFAZ transmission boundary",
                        "<signed/>",
                        new SefazTransmissionResponseDto(
                                SefazTransmissionStatus.AUTHORIZED,
                                103,
                                "Lote recebido com sucesso",
                                null,
                                new SefazAuthorizationResponseDto(
                                        SefazAuthorizationStatus.AUTHORIZED,
                                        100,
                                        "Autorizado o uso da NF-e",
                                        "135260000000123",
                                        "35260912345678000199550030000010001000000018",
                                        "dGVzdA=="))));

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestWithProcessingMode("EMIT", "")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.processingMode").value("EMIT"))
                .andExpect(jsonPath("$.status").value("AUTHORIZED"))
                .andExpect(jsonPath("$.transmission.status").value("AUTHORIZED"))
                .andExpect(jsonPath("$.transmission.code").value(103))
                .andExpect(jsonPath("$.transmission.message").value("Lote recebido com sucesso"))
                .andExpect(jsonPath("$.transmission.authorization.status").value("AUTHORIZED"))
                .andExpect(jsonPath("$.transmission.authorization.code").value(100))
                .andExpect(jsonPath("$.transmission.authorization.message").value("Autorizado o uso da NF-e"))
                .andExpect(jsonPath("$.transmission.authorization.protocol").value("135260000000123"))
                .andExpect(jsonPath("$.transmission.authorization.accessKey")
                        .value("35260912345678000199550030000010001000000018"))
                .andExpect(jsonPath("$.transmission.authorization.digestValue").value("dGVzdA=="))
                .andExpect(jsonPath("$.transmission.rawResponse").doesNotExist());
    }

    @Test
    void shouldReturnRejectedEmitResponseWithAuthorizationDetails() throws Exception {
        when(nfeEmissionService.issue(any()))
                .thenReturn(new NfeEmissionResponse("emission-1",
                        ProcessingMode.EMIT, ProcessingStatus.REJECTED,
                        "NF-e signed and submitted to the SEFAZ transmission boundary",
                        "<signed/>",
                        new SefazTransmissionResponseDto(
                                SefazTransmissionStatus.REJECTED,
                                103,
                                "Lote recebido com sucesso",
                                null,
                                new SefazAuthorizationResponseDto(
                                        SefazAuthorizationStatus.REJECTED,
                                        110,
                                        "Uso Denegado",
                                        "135260000000124",
                                        null,
                                        null))));

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestWithProcessingMode("EMIT", "")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.processingMode").value("EMIT"))
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.transmission.status").value("REJECTED"))
                .andExpect(jsonPath("$.transmission.code").value(103))
                .andExpect(jsonPath("$.transmission.authorization.status").value("REJECTED"))
                .andExpect(jsonPath("$.transmission.authorization.code").value(110))
                .andExpect(jsonPath("$.transmission.authorization.message").value("Uso Denegado"))
                .andExpect(jsonPath("$.transmission.rawResponse").doesNotExist());
    }

    @Test
    void shouldReturnProcessingEmitResponseWithReceiptAndNoAuthorization() throws Exception {
        when(nfeEmissionService.issue(any()))
                .thenReturn(new NfeEmissionResponse("emission-1",
                        ProcessingMode.EMIT, ProcessingStatus.PROCESSING,
                        "NF-e signed and submitted to the SEFAZ transmission boundary",
                        "<signed/>",
                        new SefazTransmissionResponseDto(
                                SefazTransmissionStatus.PROCESSING,
                                103,
                                "Lote recebido com sucesso",
                                "351000012345678",
                                null)));

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestWithProcessingMode("EMIT", "")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.processingMode").value("EMIT"))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.transmission.status").value("PROCESSING"))
                .andExpect(jsonPath("$.transmission.code").value(103))
                .andExpect(jsonPath("$.transmission.receipt").value("351000012345678"))
                .andExpect(jsonPath("$.transmission.authorization").doesNotExist())
                .andExpect(jsonPath("$.transmission.rawResponse").doesNotExist());
    }

    @Test
    void shouldReturnBadRequestWhenProcessingModeIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "externalReference": "REF-001",
                                  "operationType": "TRANSFER",
                                  "issuer": { "name": "Acme Ltd", "document": "12345678000199" },
                                  "recipient": { "name": "Beta Corp", "document": "98765432000188" },
                                  "items": [ { "productCode": "P-1", "description": "Widget", "ncm": "84818090", "unit": "UN", "quantity": 1, "unitValue": 10.50, "totalValue": 10.50, "cfop": "5102", "origin": "0" } ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'processingMode')]").exists());

        verifyNoInteractions(nfeEmissionService);
    }

    @Test
    void shouldReturnBadRequestWhenProcessingModeIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestWithProcessingMode("SIGNED", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(nfeEmissionService);
    }

    @Test
    void shouldReturnInternalServerErrorWhenSigningIsNotConfigured() throws Exception {
        when(nfeEmissionService.issue(any()))
                .thenThrow(new SigningNotConfiguredException(
                        "EMIT processing mode requires signing credentials, but no XmlSigner is configured"));

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestWithProcessingMode("EMIT", "")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(
                        "EMIT processing mode requires signing credentials, but no XmlSigner is configured"));
    }

    @Test
    void shouldRejectTransferRequestWithImportDetails() throws Exception {
        when(nfeEmissionService.issue(any()))
                .thenThrow(new IllegalArgumentException("importDetails and exportDetails must be absent for TRANSFER"));

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest("TRANSFER", IMPORT_SECTION)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("importDetails and exportDetails must be absent for TRANSFER"));
    }

    @Test
    void shouldRejectTransferRequestWithExportDetails() throws Exception {
        when(nfeEmissionService.issue(any()))
                .thenThrow(new IllegalArgumentException("importDetails and exportDetails must be absent for TRANSFER"));

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest("TRANSFER", EXPORT_SECTION)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("importDetails and exportDetails must be absent for TRANSFER"));
    }

    @Test
    void shouldReturnBadRequestWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "operationType": "TRANSFER",
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'externalReference')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'issuer')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'recipient')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'items')]").exists());

        verifyNoInteractions(nfeEmissionService);
    }

    @Test
    void shouldReturnBadRequestWhenNestedFieldsAreInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "externalReference": "REF-1",
                                  "operationType": "IMPORT",
                                  "issuer": { "name": "", "document": "12345678000199" },
                                  "recipient": { "name": "Beta Corp", "document": "" },
                                  "items": [
                                    { "description": "", "quantity": 0, "unitValue": -1 }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'issuer.name')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'recipient.document')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'items[0].description')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'items[0].quantity')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'items[0].unitValue')]").exists());

        verifyNoInteractions(nfeEmissionService);
    }

    @Test
    void shouldReturnBadRequestWhenOperationTypeIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "externalReference": "REF-1",
                                  "operationType": "UNKNOWN",
                                  "issuer": { "name": "Acme Ltd", "document": "12345678000199" },
                                  "recipient": { "name": "Beta Corp", "document": "98765432000188" },
                                  "items": [
                                    { "productCode": "P-1", "description": "Widget", "ncm": "84818090", "unit": "UN", "quantity": 1, "unitValue": 10.50, "totalValue": 10.50, "cfop": "5102", "origin": "0" }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(nfeEmissionService);
    }

    @Test
    void shouldReturnMethodNotAllowedForUnsupportedHttpMethod() throws Exception {
        mockMvc.perform(get("/api/v1/nfe"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.message").value("Method Not Allowed"));

        verifyNoInteractions(nfeEmissionService);
    }

    @Test
    void shouldReturnNotFoundForUnknownEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"));

        verifyNoInteractions(nfeEmissionService);
    }

    @Test
    void shouldReturnInternalServerErrorForUnexpectedException() throws Exception {
        when(nfeEmissionService.issue(any())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest("IMPORT", IMPORT_SECTION)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }

    @Test
    void shouldAcceptIssuerAndRecipientAddresses() throws Exception {
        stubReceivedResponse();

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestWithAddresses()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("XML_GENERATED"));
    }

    @Test
    void shouldAcceptItemTributaryFields() throws Exception {
        stubReceivedResponse();

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestWithItemTributaryFields()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("XML_GENERATED"));
    }

    @Test
    void shouldAcceptItemTaxation() throws Exception {
        stubReceivedResponse();

        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestWithItemTaxation()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("XML_GENERATED"));
    }

    @Test
    void shouldRejectAddressWithInvalidMunicipalityCode() throws Exception {
        // Structural validation lives in the domain Address value object:
        // a non-7-digit municipalityCode fails during deserialization and
        // surfaces as a malformed-request 400. The XML/XSD layer remains the
        // authoritative final structural boundary.
        mockMvc.perform(post("/api/v1/nfe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestWithIssuerAddress("\"municipalityCode\": \"12\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    private void stubReceivedResponse() {
        when(nfeEmissionService.issue(any()))
                .thenReturn(new NfeEmissionResponse("emission-1",
                        ProcessingMode.XML_ONLY, ProcessingStatus.XML_GENERATED,
                        XML_GENERATED_MESSAGE, "<NFe/>", null));
    }

    private String jsonRequestWithItemTaxation() {
        return """
                {
                  "externalReference": "REF-001",
                  "operationType": "TRANSFER",
                  "processingMode": "XML_ONLY",
                  "issuer": { "name": "Acme Ltd", "document": "12345678000199" },
                  "recipient": { "name": "Beta Corp", "document": "98765432000188" },
                  "items": [ { "productCode": "P-1", "description": "Widget", "ncm": "84818090",
                    "unit": "UN", "quantity": 1, "unitValue": 10.50, "totalValue": 10.50,
                    "cfop": "5102", "origin": "0",
                    "taxation": {
                      "icms": { "cst": "00", "modBc": "3", "taxBase": 10.50, "taxRate": 18.00, "taxAmount": 1.89 },
                      "ipi": { "cst": "50", "taxBase": 10.50, "taxRate": 5.00, "taxAmount": 0.53 },
                      "pisCofins": { "pisCst": "01", "pisBase": 10.50, "pisRate": 1.65, "pisAmount": 0.17,
                        "cofinsCst": "01", "cofinsBase": 10.50, "cofinsRate": 7.60, "cofinsAmount": 0.80 },
                      "importTax": { "taxBase": 10.50, "taxAmount": 1.05, "customsExpenses": 0.10, "iofAmount": 0.05 },
                      "is": { "cst": "1", "cClassTrib": "01", "taxBase": 10.50, "rate": 5.00,
                        "taxableUnit": "UN", "taxableQuantity": 1, "amount": 0.53 },
                      "rtc": { "cst": "1", "cClassTrib": "01", "indDoacao": "0",
                        "ibsCbs": { "taxBase": 10.50, "ibsAmount": 0.95,
                          "ibsUf": { "rate": 8.80, "amount": 0.92 },
                          "ibsMunicipality": { "rate": 0.20, "amount": 0.02 } } } } } ]
                }
                """;
    }

    private String jsonRequestWithItemTributaryFields() {
        return """
                {
                  "externalReference": "REF-001",
                  "operationType": "TRANSFER",
                  "processingMode": "XML_ONLY",
                  "issuer": { "name": "Acme Ltd", "document": "12345678000199" },
                  "recipient": { "name": "Beta Corp", "document": "98765432000188" },
                  "items": [ { "productCode": "P-1", "description": "Widget", "ncm": "84818090",
                    "cEan": "7891234567895", "cEanTrib": "7899876543210",
                    "unit": "UN", "quantity": 1, "unitValue": 10.50, "totalValue": 10.50,
                    "tributaryUnit": "UN", "tributaryQuantity": 1, "tributaryUnitValue": 10.50,
                    "cfop": "5102", "origin": "0", "indTot": true } ]
                }
                """;
    }

    private String jsonRequestWithAddresses() {
        return jsonRequestWithIssuerAddress(
                "\"street\": \"Main St\", \"number\": \"100\", \"city\": \"Sao Paulo\", \"state\": \"SP\", "
                        + "\"zipCode\": \"01310100\", \"neighborhood\": \"Centro\", \"municipalityCode\": \"3550308\"");
    }

    private String jsonRequestWithIssuerAddress(String issuerAddressJson) {
        return """
                {
                  "externalReference": "REF-001",
                  "operationType": "TRANSFER",
                  "processingMode": "XML_ONLY",
                  "issuer": { "name": "Acme Ltd", "document": "12345678000199",
                    "tradeName": "Acme Comercio Ltda", "taxRegime": "3",
                    "address": { %s } },
                  "recipient": { "name": "Beta Corp", "document": "98765432000188",
                    "address": { "street": "Rua B", "number": "200", "city": "Sao Paulo", "state": "SP", "zipCode": "01310200", "neighborhood": "Centro", "municipalityCode": "3550308" } },
                  "items": [ { "productCode": "P-1", "description": "Widget", "ncm": "84818090", "unit": "UN", "quantity": 1, "unitValue": 10.50, "totalValue": 10.50, "cfop": "5102", "origin": "0" } ]
                }
                """.formatted(issuerAddressJson);
    }

    private String jsonRequest(String operationType, String sections) {
        return """
                {
                  "externalReference": "REF-001",
                  "operationType": "%s",
                  "processingMode": "XML_ONLY",
                  "issuer": { "name": "Acme Ltd", "document": "12345678000199" },
                  "recipient": { "name": "Beta Corp", "document": "98765432000188" },
                  "items": [ { "productCode": "P-1", "description": "Widget", "ncm": "84818090", "unit": "UN", "quantity": 1, "unitValue": 10.50, "totalValue": 10.50, "cfop": "5102", "origin": "0" } ]%s
                }
                """.formatted(operationType, sections);
    }

    private String jsonRequestWithProcessingMode(String processingMode, String sections) {
        return """
                {
                  "externalReference": "REF-001",
                  "operationType": "TRANSFER",
                  "processingMode": "%s",
                  "issuer": { "name": "Acme Ltd", "document": "12345678000199" },
                  "recipient": { "name": "Beta Corp", "document": "98765432000188" },
                  "items": [ { "productCode": "P-1", "description": "Widget", "ncm": "84818090", "unit": "UN", "quantity": 1, "unitValue": 10.50, "totalValue": 10.50, "cfop": "5102", "origin": "0" } ]%s
                }
                """.formatted(processingMode, sections);
    }

    private String jsonRequestWithHeader(String operationType, String sections) {
        return """
                {
                  "externalReference": "REF-001",
                  "operationType": "%s",
                  "processingMode": "XML_ONLY",
                  "model": "55",
                  "series": 3,
                  "number": 1000,
                  "emissionDate": "2026-09-21T10:30:00-03:00",
                  "operationDescription": "Remessa para industrializa\u00e7\u00e3o",
                  "fiscalEstablishmentCity": "3550308",
                  "issuer": { "name": "Acme Ltd", "document": "12345678000199" },
                  "recipient": { "name": "Beta Corp", "document": "98765432000188" },
                  "items": [ { "productCode": "P-1", "description": "Widget", "ncm": "84818090", "unit": "UN", "quantity": 1, "unitValue": 10.50, "totalValue": 10.50, "cfop": "5102", "origin": "0" } ]%s
                }
                """.formatted(operationType, sections);
    }

    private String jsonRequestWithIdentification(String operationType, String sections) {
        return """
                {
                  "externalReference": "REF-001",
                  "operationType": "%s",
                  "processingMode": "XML_ONLY",
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
                  "issuer": { "name": "Acme Ltd", "document": "12345678000199" },
                  "recipient": { "name": "Beta Corp", "document": "98765432000188" },
                  "items": [ { "productCode": "P-1", "description": "Widget", "ncm": "84818090", "unit": "UN", "quantity": 1, "unitValue": 10.50, "totalValue": 10.50, "cfop": "5102", "origin": "0" } ]%s
                }
                """.formatted(operationType, sections);
    }

    private String jsonRequestWithIssuerIdentity(String operationType, String sections) {
        return """
                {
                  "externalReference": "REF-001",
                  "operationType": "%s",
                  "processingMode": "XML_ONLY",
                  "issuer": { "name": "Acme Ltd", "document": "12345678000199", "tradeName": "Acme Comercio Ltda", "taxRegime": "3" },
                  "recipient": { "name": "Beta Corp", "document": "98765432000188" },
                  "items": [ { "productCode": "P-1", "description": "Widget", "ncm": "84818090", "unit": "UN", "quantity": 1, "unitValue": 10.50, "totalValue": 10.50, "cfop": "5102", "origin": "0" } ]%s
                }
                """.formatted(operationType, sections);
    }

    private String jsonRequestWithRecipientIeStatus(String operationType, String sections) {
        return """
                {
                  "externalReference": "REF-001",
                  "operationType": "%s",
                  "processingMode": "XML_ONLY",
                  "issuer": { "name": "Acme Ltd", "document": "12345678000199" },
                  "recipient": { "name": "Beta Corp", "document": "98765432000188", "ieStatus": "CONTRIBUTOR" },
                  "items": [ { "productCode": "P-1", "description": "Widget", "ncm": "84818090", "unit": "UN", "quantity": 1, "unitValue": 10.50, "totalValue": 10.50, "cfop": "5102", "origin": "0" } ]%s
                }
                """.formatted(operationType, sections);
    }

    private String jsonRequestWithPayment(String operationType, String sections) {
        return """
                {
                  "externalReference": "REF-001",
                  "operationType": "%s",
                  "processingMode": "XML_ONLY",
                  "issuer": { "name": "Acme Ltd", "document": "12345678000199" },
                  "recipient": { "name": "Beta Corp", "document": "98765432000188" },
                  "items": [ { "productCode": "P-1", "description": "Widget", "ncm": "84818090", "unit": "UN", "quantity": 1, "unitValue": 10.50, "totalValue": 10.50, "cfop": "5102", "origin": "0" } ],
                  "payment": { "details": [ { "paymentMethod": "17", "amount": 10.50, "indicator": "IMMEDIATE" } ] }%s
                }
                """.formatted(operationType, sections);
    }
}
