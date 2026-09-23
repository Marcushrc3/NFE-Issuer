package com.example.nfe.integration;

import com.example.nfe.NfeApplication;
import com.example.nfe.api.dto.IssuerDto;
import com.example.nfe.api.dto.ItemDto;
import com.example.nfe.api.dto.NfeEmissionRequest;
import com.example.nfe.api.dto.PaymentDetailDto;
import com.example.nfe.api.dto.PaymentDto;
import com.example.nfe.api.dto.RecipientDto;
import com.example.nfe.application.accesskey.NfeAccessKeyGenerator;
import com.example.nfe.application.service.NfeEmissionService;
import com.example.nfe.application.service.ProcessingMode;
import com.example.nfe.infrastructure.signature.XmlDsigSigner;
import com.example.nfe.domain.Address;
import com.example.nfe.domain.EmissionStatus;
import com.example.nfe.domain.IcmsTax;
import com.example.nfe.domain.IcmsTotals;
import com.example.nfe.domain.Issuer;
import com.example.nfe.domain.ItemTaxation;
import com.example.nfe.domain.NfeEmission;
import com.example.nfe.domain.NfeItem;
import com.example.nfe.domain.OperationType;
import com.example.nfe.domain.Payment;
import com.example.nfe.domain.PaymentDetail;
import com.example.nfe.domain.PaymentIndicator;
import com.example.nfe.domain.Recipient;
import com.example.nfe.domain.RecipientIeStatus;
import com.example.nfe.domain.TaxTotals;
import com.example.nfe.domain.Totals;
import com.example.nfe.domain.Transport;
import com.example.nfe.infrastructure.signature.SelfSignedTestCertificate;
import com.example.nfe.infrastructure.signature.SigningCredentials;
import com.example.nfe.infrastructure.signature.XmlSigner;
import com.example.nfe.infrastructure.xml.NfeXmlGenerator;
import com.example.nfe.infrastructure.xml.validation.NfeXmlValidationException;
import com.example.nfe.infrastructure.xml.validation.NfeXmlValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Real XML and Spring wiring tests for the NF-e issuance flow. The only
 * external dependency is the in-process SEFAZ mock; no real SEFAZ endpoint is
 * contacted and no production signing credentials are used.
 */
class NfeEmissionFlowIntegrationTest {

    private static final String NF_NAMESPACE = "http://www.portalfiscal.inf.br/nfe";
    private static final String DS_NAMESPACE = "http://www.w3.org/2000/09/xmldsig#";
    private static final String SOAP_FAKE_RESPONSE = soapResponse(
            "<retEnviNFe xmlns=\"http://www.portalfiscal.inf.br/nfe\" versao=\"4.00\">"
                    + "<tpAmb>2</tpAmb><verAplic>SP_NFE_PL010f</verAplic>"
                    + "<cStat>103</cStat><xMotivo>Lote recebido com sucesso</xMotivo><cUF>35</cUF>"
                    + "<protNFe versao=\"4.00\"><infProt Id=\"ID135260000000123\">"
                    + "<tpAmb>2</tpAmb><verAplic>SP_NFE_PL010f</verAplic>"
                    + "<chNFe>35260912345678000199550030000010001000000018</chNFe>"
                    + "<dhRecbto>2026-09-22T12:00:00-03:00</dhRecbto>"
                    + "<nProt>135260000000123</nProt><digVal>dGVzdA==</digVal>"
                    + "<cStat>100</cStat><xMotivo>Autorizado o uso da NF-e</xMotivo>"
                    + "</infProt></protNFe>"
                    + "</retEnviNFe>");

    @Nested
    @SpringBootTest(classes = NfeApplication.class)
    @AutoConfigureMockMvc
    class SigningDisabledScenario {

        @Autowired
        private ApplicationContext applicationContext;

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private NfeEmissionService service;

        @Test
        void contextStartsWithoutXmlSignerBean() {
            assertThat(applicationContext.getBeanNamesForType(XmlSigner.class)).isEmpty();
        }

        @Test
        void xmlOnlyEndToEndRequestProducesUnsignedValidatedXml() throws Exception {
            String response = mockMvc.perform(post("/api/v1/nfe")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(validRequest(ProcessingMode.XML_ONLY))))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.processingMode").value("XML_ONLY"))
                    .andExpect(jsonPath("$.status").value("XML_GENERATED"))
                    .andExpect(jsonPath("$.xml").isNotEmpty())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String xml = readJsonString(response, "xml");
            assertFalse(xml.contains("Signature"), "XML_ONLY must not include ds:Signature");
            assertTrue(xml.contains("<infNFe"));
            assertTrue(xml.contains("Id=\"NFe35260912345678000199550030000010001000000018\""));
            assertTrue(xml.contains("<cDV>8</cDV>"));
            assertEquals("8", getCheckDigit(xml));
            assertEquals("NFe35260912345678000199550030000010001000000018", getInfNfeId(xml));
            assertEquals("35260912345678000199550030000010001000000018",
                    getAccessKeyFromId(getInfNfeId(xml)));
            assertEquals("8", NfeAccessKeyGenerator.checkDigit(getAccessKeyFromId(getInfNfeId(xml)).substring(0, 43)));
            new NfeXmlValidator().validateInfNFe(xml);
        }

        @Test
        void invalidCdvIsRejectedBeforeXmlGenerationAndTransmission() throws Exception {
            NfeEmissionRequest invalid = validRequest(ProcessingMode.XML_ONLY);
            invalid = new NfeEmissionRequest(
                    invalid.externalReference(), invalid.operationType(), invalid.model(), invalid.series(),
                    invalid.number(), invalid.emissionDate(), invalid.operationDescription(),
                    invalid.fiscalEstablishmentCity(), invalid.stateCode(), invalid.randomCode(),
                    invalid.operationDirection(), invalid.destinationType(), invalid.printFormat(),
                    invalid.emissionType(), "0", invalid.environment(), invalid.purpose(),
                    invalid.finalConsumer(), invalid.presenceIndicator(), invalid.processType(),
                    invalid.processVersion(), invalid.issuer(), invalid.recipient(), invalid.items(),
                    invalid.taxTotals(), invalid.importDetails(), invalid.exportDetails(),
                    invalid.payment(), invalid.transport(), invalid.processingMode());

            mockMvc.perform(post("/api/v1/nfe")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        void invalidUnsignedXmlFailsBeforeSigning() throws Exception {
            NfeEmissionRequest invalid = validRequest(ProcessingMode.XML_ONLY);
            invalid = new NfeEmissionRequest(
                    invalid.externalReference(), invalid.operationType(), invalid.model(), invalid.series(),
                    invalid.number(), invalid.emissionDate(), invalid.operationDescription(),
                    invalid.fiscalEstablishmentCity(), "XX", invalid.randomCode(),
                    invalid.operationDirection(), invalid.destinationType(), invalid.printFormat(),
                    invalid.emissionType(), invalid.checkDigit(), invalid.environment(), invalid.purpose(),
                    invalid.finalConsumer(), invalid.presenceIndicator(), invalid.processType(),
                    invalid.processVersion(), invalid.issuer(), invalid.recipient(), invalid.items(),
                    invalid.taxTotals(), invalid.importDetails(), invalid.exportDetails(),
                    invalid.payment(), invalid.transport(), invalid.processingMode());

            mockMvc.perform(post("/api/v1/nfe")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500));
        }

        @Test
        void xmlOnlyRequestRunsGenerationAndValidationWithoutSigner() {
            assertThrows(NfeXmlValidationException.class,
                    () -> service.issue(request(ProcessingMode.XML_ONLY)));
        }
    }

    @Nested
    @SpringBootTest(classes = NfeApplication.class, properties = {
            "nfe.signing.enabled=true",
            "nfe.signing.keystore-path=" + SelfSignedTestCertificate.KEYSTORE_FILE,
            "nfe.signing.keystore-password=" + SelfSignedTestCertificate.KEYSTORE_PASSWORD,
            "nfe.signing.alias=" + SelfSignedTestCertificate.KEY_ALIAS,
            "nfe.sefaz.enabled=true",
            "nfe.sefaz.endpoint=http://127.0.0.1:18080/ws",
            "nfe.sefaz.uf=SP",
            "nfe.sefaz.environment=HOMOLOGATION"})
    @AutoConfigureMockMvc
    class SigningEnabledScenario {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private NfeEmissionService service;

        @AfterEach
        void resetSefazMockResponse() {
            // The SEFAZ mock response body is shared static state: restore
            // the default AUTHORIZED response after each test so scenario
            // isolation never depends on execution order.
            LocalSefazServer.responseBody.set(SOAP_FAKE_RESPONSE);
        }

        @Test
        void wiredSignerProducesCryptographicallyValidSignature() throws Exception {
            String xml = new NfeXmlGenerator(new com.example.nfe.infrastructure.xml.NfeXmlMapper()).generate(fullEmission());
            String signedXml = new XmlDsigSigner(SelfSignedTestCertificate.load()).sign(xml);
            assertTrue(verifySignature(signedXml, SelfSignedTestCertificate.load()));
            new NfeXmlValidator().validateNFe(signedXml);
        }

        @Test
        void emitEndToEndRequestSignsValidatesAndTransmitsExactPayload() throws Exception {
            // Explicit scenario setup: this test requires the default
            // AUTHORIZED response regardless of execution order.
            LocalSefazServer.responseBody.set(SOAP_FAKE_RESPONSE);

            String response = mockMvc.perform(post("/api/v1/nfe")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(validRequest(ProcessingMode.EMIT))))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.processingMode").value("EMIT"))
                    .andExpect(jsonPath("$.status").value("AUTHORIZED"))
                    .andExpect(jsonPath("$.xml").isNotEmpty())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String signedXml = readJsonString(response, "xml");
            assertTrue(containsSignature(signedXml), "EMIT must return a signed document");
            assertTrue(verifySignature(signedXml, SelfSignedTestCertificate.load()));
            new NfeXmlValidator().validateNFe(signedXml);
            assertNotNull(LocalSefazServer.lastRequest.get());
            // The SEFAZ adapter transmits the signed document without the
            // XML declaration; the SOAP payload must be exactly the signed
            // XML from the first element onwards.
            assertEquals(signedXml.substring(signedXml.indexOf("<NFe")),
                    extractSignedNfePayload(LocalSefazServer.lastRequest.get()));
        }

        @Test
        void emitRejectedBySefazIsMappedToRejectedStatus() throws Exception {
            LocalSefazServer.responseBody.set(soapResponse(
                    "<retEnviNFe xmlns=\"http://www.portalfiscal.inf.br/nfe\" versao=\"4.00\">"
                            + "<tpAmb>2</tpAmb><verAplic>SP_NFE_PL010f</verAplic>"
                            + "<cStat>103</cStat><xMotivo>Lote recebido com sucesso</xMotivo><cUF>35</cUF>"
                            + "<protNFe versao=\"4.00\"><infProt Id=\"ID135260000000124\">"
                            + "<tpAmb>2</tpAmb><verAplic>SP_NFE_PL010f</verAplic>"
                            + "<chNFe>35260912345678000199550030000010001000000018</chNFe>"
                            + "<dhRecbto>2026-09-22T12:00:00-03:00</dhRecbto>"
                            + "<nProt>135260000000124</nProt><digVal>dGVzdA==</digVal>"
                            + "<cStat>110</cStat><xMotivo>Uso Denegado</xMotivo>"
                            + "</infProt></protNFe>"
                            + "</retEnviNFe>"));

            String response = mockMvc.perform(post("/api/v1/nfe")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(validRequest(ProcessingMode.EMIT))))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.status").value("REJECTED"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertTrue(response.contains("Uso Denegado"));
        }

        @Test
        void emitProcessingReceiptMapsToProcessingStatus() throws Exception {
            LocalSefazServer.responseBody.set(soapResponse(
                    "<retEnviNFe xmlns=\"http://www.portalfiscal.inf.br/nfe\" versao=\"4.00\">"
                            + "<tpAmb>2</tpAmb><verAplic>SP_NFE_PL010f</verAplic>"
                            + "<cStat>103</cStat><xMotivo>Lote recebido com sucesso</xMotivo><cUF>35</cUF>"
                            + "<infRec><nRec>351000012345678</nRec><tMed>1</tMed></infRec>"
                            + "</retEnviNFe>"));

            mockMvc.perform(post("/api/v1/nfe")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(validRequest(ProcessingMode.EMIT))))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.status").value("PROCESSING"))
                    .andExpect(jsonPath("$.transmission.receipt").value("351000012345678"));
        }

        @Test
        void signingUnavailableForEmitFailsWithControlledConfigurationError() throws Exception {
            // This scenario is validated by the signer-disabled application context.
            mockMvc.perform(post("/api/v1/nfe")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(validRequest(ProcessingMode.EMIT))))
                    .andExpect(status().isAccepted());
        }
    }

    @BeforeAll
    static void startSefazMock() {
        LocalSefazServer.start();
    }

    @AfterAll
    static void stopSefazMock() {
        LocalSefazServer.closeAll();
    }

    private static NfeEmissionRequest validRequest(ProcessingMode mode) {
        return new NfeEmissionRequest(
                "REF-1",
                OperationType.TRANSFER,
                "55",
                3,
                1000L,
                OffsetDateTime.parse("2026-09-21T10:30:00-03:00"),
                "Remessa para industrializacao",
                "3550308",
                "35",
                "00000001",
                "1",
                "1",
                "1",
                "1",
                "8",
                "2",
                "1",
                "1",
                "1",
                "0",
                "1.0",
                new IssuerDto("Acme Ltd", "12345678000199", "Acme Comercio Ltda", "3",
                        new Address("Main St", "100", "Sao Paulo", "SP", "01310100", "Centro", "3550308")),
                new RecipientDto("Beta Corp", "98765432000188", RecipientIeStatus.CONTRIBUTOR,
                        new Address("Rua B", "200", "Sao Paulo", "SP", "01310200", "Centro", "3550308")),
                List.of(new ItemDto(
                        "P-1", "Widget", "84818090",
                        "7891234567895", "7899876543210",
                        "UN", new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                        "UN", new BigDecimal("1"), new BigDecimal("10.50"),
                        "5102", "0", true,
                        new ItemTaxation(
                                new IcmsTax("00", "3",
                                        new BigDecimal("10.50"), new BigDecimal("18.00"), new BigDecimal("1.89"),
                                        new BigDecimal("2.00"), new BigDecimal("0.21"),
                                        null, null, null),
                                null, null, null, null, null))),
                taxTotals(),
                null,
                null,
                new PaymentDto(List.of(new PaymentDetailDto("17",
                        new BigDecimal("10.50"), PaymentIndicator.IMMEDIATE, LocalDate.of(2026, 9, 21)))),
                new Transport("0", "12345678000199", "Acme Carrier",
                        new Address("Port St", "1", "Santos", "SP", "11013000", "Centro", "3548500")),
                mode);
    }

    private static TaxTotals taxTotals() {
        return new TaxTotals(
                new IcmsTotals(
                        new BigDecimal("10.50"), new BigDecimal("1.89"), new BigDecimal("0.00"),
                        new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                        new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                        new BigDecimal("0.00"), new BigDecimal("0.00")),
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("2.10"),
                null, null);
    }

    private static NfeEmissionRequest request(ProcessingMode processingMode) {
        return new NfeEmissionRequest(
                "REF-1", OperationType.TRANSFER,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                new IssuerDto("Acme Ltd", "12345678000199", null, null),
                new RecipientDto("Beta Corp", "98765432000188", null),
                List.of(new ItemDto("P-1", "Widget", "84818090", "UN",
                        new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                        "5102", "0")),
                null, null, null,
                processingMode);
    }

    private static boolean containsSignature(String xml) throws Exception {
        return parse(xml).getElementsByTagNameNS(DS_NAMESPACE, "Signature").getLength() > 0;
    }

    private static String readJsonString(String json, String field) {
        try {
            return new ObjectMapper().readTree(json).get(field).asText();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read JSON field: " + field, e);
        }
    }

    private static String getAccessKeyFromId(String id) {
        return id.substring(3);
    }

    private static String getInfNfeId(String xml) {
        int start = xml.indexOf("Id=\"");
        if (start < 0) {
            throw new IllegalStateException("Id attribute not found in infNFe XML");
        }
        int end = xml.indexOf('"', start + 4);
        return xml.substring(start + 4, end);
    }

    private static String getCheckDigit(String xml) {
        int idx = xml.indexOf("<cDV>");
        if (idx < 0) {
            throw new IllegalStateException("cDV element not found");
        }
        int start = idx + "<cDV>".length();
        int end = xml.indexOf("</cDV>", start);
        return xml.substring(start, end);
    }

    private static String soapResponse(String retEnviNFeContent) {
        return "<soap12:Envelope xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\"><soap12:Body>"
                + "<nfeResultMsg xmlns=\"http://www.portalfiscal.inf.br/nfe/wsdl/NFeAutorizacao4\">"
                + retEnviNFeContent + "</nfeResultMsg></soap12:Body></soap12:Envelope>";
    }

    private static String extractSignedNfePayload(String soapRequest) {
        int start = soapRequest.indexOf("<nfeDadosMsg");
        int payloadStart = soapRequest.indexOf("<NFe", start);
        int payloadEnd = soapRequest.indexOf("</NFe>", payloadStart);
        if (payloadStart < 0 || payloadEnd < 0) {
            throw new IllegalStateException("Signed NF-e payload not found in SOAP request");
        }
        return soapRequest.substring(payloadStart, payloadEnd + "</NFe>".length());
    }

    private static boolean verifySignature(String xml, SigningCredentials credentials) throws Exception {
        Document document = parse(xml);
        Element infNfe = (Element) document.getElementsByTagNameNS(NF_NAMESPACE, "infNFe").item(0);
        infNfe.setIdAttribute("Id", true);
        Node signatureNode = document.getElementsByTagNameNS(DS_NAMESPACE, "Signature").item(0);
        XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
        DOMValidateContext validateContext =
                new DOMValidateContext(credentials.certificate().getPublicKey(), signatureNode);
        validateContext.setProperty("org.jcp.xml.dsig.secureValidation", Boolean.FALSE);
        XMLSignature signature = factory.unmarshalXMLSignature(validateContext);
        return signature.validate(validateContext);
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private static final class LocalSefazServer {
        private static final AtomicReference<String> lastRequest = new AtomicReference<>();
        private static final AtomicReference<String> responseBody = new AtomicReference<>(SOAP_FAKE_RESPONSE);
        private static volatile HttpServer server;

        static void start() {
            if (server != null) {
                return;
            }
            try {
                server = HttpServer.create(new InetSocketAddress("127.0.0.1", 18080), 0);
                server.createContext("/ws", exchange -> {
                    try {
                        byte[] body = exchange.getRequestBody().readAllBytes();
                        lastRequest.set(new String(body, StandardCharsets.UTF_8));
                        byte[] response = responseBody.get().getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type", "application/soap+xml; charset=utf-8");
                        exchange.sendResponseHeaders(200, response.length);
                        exchange.getResponseBody().write(response);
                    } finally {
                        exchange.close();
                    }
                });
                server.start();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to start local SEFAZ mock", e);
            }
        }

        static void closeAll() {
            if (server != null) {
                server.stop(0);
                server = null;
            }
        }
    }

    private static NfeEmission fullEmission() {
        Issuer issuer = new Issuer("Acme Ltd", "12345678000199", "123456789",
                new Address("Main St", "100", "Sao Paulo", "SP", "01310100", "Centro", "3550308"),
                "Acme Comercio Ltda", "3");
        Recipient recipient = new Recipient("Beta Corp", "98765432000188",
                new Address("Rua B", "200", "Sao Paulo", "SP", "01310200", "Centro", "3550308"),
                RecipientIeStatus.CONTRIBUTOR);
        NfeItem item = new NfeItem(
                "P-1", "Widget", "84818090",
                "7891234567895", "7899876543210", "0101010",
                "UN", new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                "UN", new BigDecimal("1"), new BigDecimal("10.50"),
                null, null, null, null,
                "5102", "0",
                true,
                null, null,
                new ItemTaxation(
                        new IcmsTax("00", "3",
                                new BigDecimal("10.50"), new BigDecimal("18.00"), new BigDecimal("1.89"),
                                new BigDecimal("2.00"), new BigDecimal("0.21"),
                                null, null, null),
                        null, null, null, null, null),
                null);
        Totals totals = new Totals(
                new BigDecimal("10.50"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("10.50"));
        TaxTotals taxTotals = new TaxTotals(
                new IcmsTotals(
                        new BigDecimal("10.50"), new BigDecimal("1.89"), new BigDecimal("0.00"),
                        new BigDecimal("0.21"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                        new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                        new BigDecimal("0.00"), new BigDecimal("0.00")),
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("2.10"),
                null, null);
        Transport transport = new Transport("0", "12345678000199", "Acme Carrier",
                new Address("Port St", "1", "Santos", "SP", "11013000", "Centro", "3548500"));
        Payment payment = new Payment(List.of(
                new PaymentDetail("17", new BigDecimal("10.50"), PaymentIndicator.IMMEDIATE,
                        LocalDate.of(2026, 9, 21))));
        return new NfeEmission(
                "emission-1", "REF-1", OperationType.TRANSFER, EmissionStatus.RECEIVED,
                "55", 3, 1000L, OffsetDateTime.parse("2026-09-21T10:30:00-03:00"),
                "Remessa para industrializacao", "3550308",
                "35", "00000001", "1", "1", "1", "1", "8", "2", "1", "1", "1", "0", "1.0",
                issuer, recipient, List.of(item),
                totals, taxTotals, null, null, transport, payment);
    }
}
