package com.example.nfe.infrastructure.sefaz.nfe;

import com.example.nfe.application.sefaz.SefazAuthorizationResult;
import com.example.nfe.application.sefaz.SefazAuthorizationStatus;
import com.example.nfe.application.sefaz.SefazTransmissionResult;
import com.example.nfe.application.sefaz.SefazTransmissionStatus;
import com.example.nfe.infrastructure.sefaz.config.NfeSefazEnvironment;
import com.example.nfe.infrastructure.sefaz.config.NfeSefazProperties;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the SEFAZ authorization adapter. A tiny in-process HTTP
 * server (JDK {@code com.sun.net.httpserver}) plays the SEFAZ role — no
 * network access, no real SEFAZ call, no certificate is ever involved.
 */
class NfeSefazTransmitterTest {

    private static final String SIGNED_NFE =
            "<NFe xmlns=\"http://www.portalfiscal.inf.br/nfe\">"
                    + "<infNFe Id=\"NFe35260912345678000199550030000010001000000018\" versao=\"4.00\">"
                    + "<ide><cUF>35</cUF></ide></infNFe>"
                    + "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"
                    + "<SignatureValue>dGVzdHNpZ25hdHVyZQ==</SignatureValue></Signature>"
                    + "</NFe>";

    private static final String SIGNED_NFE_WITH_DECLARATION =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + SIGNED_NFE;

    private static final String SOAP_ENV_NS = "http://www.w3.org/2003/05/soap-envelope";
    private static final String WSDL_NS = "http://www.portalfiscal.inf.br/nfe/wsdl/NFeAutorizacao4";
    private static final String NFE_NS = "http://www.portalfiscal.inf.br/nfe";
    private static final String SOAP_ACTION = WSDL_NS + "/nfeAutorizacaoLote";

    private static final String AUTHORIZED_RESPONSE = soapResponse(
            "<retEnviNFe xmlns=\"" + NFE_NS + "\" versao=\"4.00\">"
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

    private static final String DENIED_RESPONSE = soapResponse(
            "<retEnviNFe xmlns=\"" + NFE_NS + "\" versao=\"4.00\">"
                    + "<tpAmb>2</tpAmb><verAplic>SP_NFE_PL010f</verAplic>"
                    + "<cStat>103</cStat><xMotivo>Lote recebido com sucesso</xMotivo><cUF>35</cUF>"
                    + "<protNFe versao=\"4.00\"><infProt Id=\"ID135260000000124\">"
                    + "<tpAmb>2</tpAmb><verAplic>SP_NFE_PL010f</verAplic>"
                    + "<chNFe>35260912345678000199550030000010001000000018</chNFe>"
                    + "<dhRecbto>2026-09-22T12:01:00-03:00</dhRecbto>"
                    + "<nProt>135260000000124</nProt><digVal>dGVzdA==</digVal>"
                    + "<cStat>110</cStat><xMotivo>Uso Denegado</xMotivo>"
                    + "</infProt></protNFe>"
                    + "</retEnviNFe>");

    private static final String LOTE_REJECTED_RESPONSE = soapResponse(
            "<retEnviNFe xmlns=\"" + NFE_NS + "\" versao=\"4.00\">"
                    + "<tpAmb>2</tpAmb><verAplic>SP_NFE_PL010f</verAplic>"
                    + "<cStat>225</cStat><xMotivo>Rejeicao: Falha no Schema XML do lote</xMotivo>"
                    + "<cUF>35</cUF>"
                    + "</retEnviNFe>");

    private static final String PROCESSING_RESPONSE = soapResponse(
            "<retEnviNFe xmlns=\"" + NFE_NS + "\" versao=\"4.00\">"
                    + "<tpAmb>2</tpAmb><verAplic>SP_NFE_PL010f</verAplic>"
                    + "<cStat>103</cStat><xMotivo>Lote recebido com sucesso</xMotivo><cUF>35</cUF>"
                    + "<infRec><nRec>351000012345678</nRec><tMed>1</tMed></infRec>"
                    + "</retEnviNFe>");

    private static final String PROCESSING_105_RESPONSE = soapResponse(
            "<retEnviNFe xmlns=\"" + NFE_NS + "\" versao=\"4.00\">"
                    + "<tpAmb>2</tpAmb><verAplic>SP_NFE_PL010f</verAplic>"
                    + "<cStat>105</cStat><xMotivo>Lote em processamento</xMotivo><cUF>35</cUF>"
                    + "</retEnviNFe>");

    private static final String LOTE_104_WITH_PROTOCOL_RESPONSE = soapResponse(
            "<retEnviNFe xmlns=\"" + NFE_NS + "\" versao=\"4.00\">"
                    + "<tpAmb>2</tpAmb><verAplic>SP_NFE_PL010f</verAplic>"
                    + "<cStat>104</cStat><xMotivo>Lote processado</xMotivo><cUF>35</cUF>"
                    + "<protNFe versao=\"4.00\"><infProt Id=\"ID135260000000125\">"
                    + "<tpAmb>2</tpAmb><verAplic>SP_NFE_PL010f</verAplic>"
                    + "<chNFe>35260912345678000199550030000010001000000018</chNFe>"
                    + "<dhRecbto>2026-09-22T12:02:00-03:00</dhRecbto>"
                    + "<nProt>135260000000125</nProt><digVal>dGVzdA==</digVal>"
                    + "<cStat>100</cStat><xMotivo>Autorizado o uso da NF-e</xMotivo>"
                    + "</infProt></protNFe>"
                    + "</retEnviNFe>");

    private static final String MISSING_CSTAT_RESPONSE = soapResponse(
            "<retEnviNFe xmlns=\"" + NFE_NS + "\" versao=\"4.00\">"
                    + "<tpAmb>2</tpAmb><xMotivo>sem status</xMotivo><cUF>35</cUF>"
                    + "</retEnviNFe>");

    private static final String XXE_RESPONSE =
            "<?xml version=\"1.0\"?>"
                    + "<!DOCTYPE retEnviNFe [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
                    + soapResponse(
                    "<retEnviNFe xmlns=\"" + NFE_NS + "\" versao=\"4.00\">"
                            + "<tpAmb>2</tpAmb><cStat>103</cStat><xMotivo>&xxe;</xMotivo>"
                            + "</retEnviNFe>");

    private static String soapResponse(String retEnviNFeContent) {
        return "<soap12:Envelope xmlns:soap12=\"" + SOAP_ENV_NS + "\"><soap12:Body>"
                + "<nfeResultMsg xmlns=\"" + WSDL_NS + "\">"
                + retEnviNFeContent
                + "</nfeResultMsg></soap12:Body></soap12:Envelope>";
    }

    @Test
    void shouldSendSoap12RequestWithExpectedNamespacesAndOperation() {
        try (LocalSefazServer server = new LocalSefazServer(AUTHORIZED_RESPONSE)) {
            transmit(server);

            String request = server.lastRequest();
            assertTrue(request.startsWith(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soap12:Envelope "
                            + "xmlns:soap12=\"" + SOAP_ENV_NS + "\">"),
                    "request must use SOAP 1.2");
            assertTrue(request.contains("<soap12:Header/>"));
            assertTrue(request.contains("<nfeDadosMsg xmlns=\"" + WSDL_NS + "\">"));
            assertTrue(request.contains("<enviNFe xmlns=\"" + NFE_NS + "\" versao=\"4.00\">"));

            Headers headers = server.lastHeaders();
            assertEquals(SOAP_ACTION, headers.getFirst("SOAPAction"));
            assertTrue(headers.getFirst("Content-Type").startsWith("application/soap+xml"));
            assertEquals("POST", server.lastMethod());
            assertEquals("/ws", server.lastPath());
        }
    }

    @Test
    void shouldWrapSignedNfeInSynchronousBatchWithoutModifyingIt() {
        try (LocalSefazServer server = new LocalSefazServer(AUTHORIZED_RESPONSE)) {
            transmit(server, SIGNED_NFE_WITH_DECLARATION);

            String request = server.lastRequest();
            assertTrue(request.matches("(?s).*<idLote>\\d{15}</idLote>.*"),
                    "idLote must be a 15-digit numeric batch identifier");
            assertTrue(request.contains("<indSinc>1</indSinc>"),
                    "authorization must be synchronous (indSinc=1)");
            assertTrue(request.contains("<indSinc>1</indSinc>" + SIGNED_NFE + "</enviNFe>"),
                    "the signed NF-e must be embedded verbatim inside the batch");
            assertEquals(-1, request.indexOf("<?xml", 1),
                    "the NF-e XML declaration must be stripped (only the SOAP declaration remains)");
        }
    }

    @Test
    void shouldMapAuthorizationResponseToAuthorizedWithProtocol() {
        try (LocalSefazServer server = new LocalSefazServer(AUTHORIZED_RESPONSE)) {
            SefazTransmissionResult result = transmit(server);

            assertEquals(SefazTransmissionStatus.AUTHORIZED, result.transmissionStatus());
            assertEquals(103, result.transmissionCode());
            assertEquals("Lote recebido com sucesso", result.transmissionMessage());
            assertNull(result.receipt());
            assertEquals(AUTHORIZED_RESPONSE, result.rawResponse());

            SefazAuthorizationResult authorization = result.authorizationResult();
            assertNotNull(authorization);
            assertEquals(SefazAuthorizationStatus.AUTHORIZED, authorization.status());
            assertEquals(100, authorization.code());
            assertEquals("Autorizado o uso da NF-e", authorization.message());
            assertEquals("135260000000123", authorization.protocol());
            assertEquals("35260912345678000199550030000010001000000018", authorization.accessKey());
            assertEquals("dGVzdA==", authorization.digestValue());
        }
    }

    @Test
    void shouldMapDeniedUsageToRejected() {
        try (LocalSefazServer server = new LocalSefazServer(DENIED_RESPONSE)) {
            SefazTransmissionResult result = transmit(server);

            assertEquals(SefazTransmissionStatus.REJECTED, result.transmissionStatus());
            assertEquals(103, result.transmissionCode());

            SefazAuthorizationResult authorization = result.authorizationResult();
            assertNotNull(authorization);
            assertEquals(SefazAuthorizationStatus.REJECTED, authorization.status());
            assertEquals(110, authorization.code());
            assertEquals("Uso Denegado", authorization.message());
            assertEquals("135260000000124", authorization.protocol());
        }
    }

    @Test
    void shouldMapLoteRejectionToRejectedWithoutProtocol() {
        try (LocalSefazServer server = new LocalSefazServer(LOTE_REJECTED_RESPONSE)) {
            SefazTransmissionResult result = transmit(server);

            assertEquals(SefazTransmissionStatus.REJECTED, result.transmissionStatus());
            assertEquals(225, result.transmissionCode());
            assertEquals("Rejeicao: Falha no Schema XML do lote", result.transmissionMessage());
            assertNull(result.authorizationResult());
            assertNull(result.receipt());
        }
    }

    @Test
    void shouldMapAsyncReceiptToProcessing() {
        try (LocalSefazServer server = new LocalSefazServer(PROCESSING_RESPONSE)) {
            SefazTransmissionResult result = transmit(server);

            assertEquals(SefazTransmissionStatus.PROCESSING, result.transmissionStatus());
            assertEquals(103, result.transmissionCode());
            assertEquals("351000012345678", result.receipt());
            assertNull(result.authorizationResult());
        }
    }

    @Test
    void shouldMapProcessingBatchStatusToProcessing() {
        try (LocalSefazServer server = new LocalSefazServer(PROCESSING_105_RESPONSE)) {
            SefazTransmissionResult result = transmit(server);

            assertEquals(SefazTransmissionStatus.PROCESSING, result.transmissionStatus());
            assertEquals(105, result.transmissionCode());
            assertEquals("Lote em processamento", result.transmissionMessage());
            assertNull(result.authorizationResult());
        }
    }

    @Test
    void shouldMapProcessedBatchWithProtocolToAuthorized() {
        try (LocalSefazServer server = new LocalSefazServer(LOTE_104_WITH_PROTOCOL_RESPONSE)) {
            SefazTransmissionResult result = transmit(server);

            assertEquals(SefazTransmissionStatus.AUTHORIZED, result.transmissionStatus());
            assertEquals(104, result.transmissionCode());
            assertEquals("Lote processado", result.transmissionMessage());

            SefazAuthorizationResult authorization = result.authorizationResult();
            assertNotNull(authorization);
            assertEquals(SefazAuthorizationStatus.AUTHORIZED, authorization.status());
            assertEquals(100, authorization.code());
            assertEquals("135260000000125", authorization.protocol());
        }
    }

    @Test
    void shouldReturnErrorOnTransportFailure() {
        int refusedPort = refusedPort();
        NfeSefazProperties properties = properties("127.0.0.1", refusedPort);
        NfeSefazTransmitter transmitter = new NfeSefazTransmitter(properties);

        SefazTransmissionResult result = transmitter.transmit(SIGNED_NFE);

        assertEquals(SefazTransmissionStatus.ERROR, result.transmissionStatus());
        assertTrue(result.transmissionMessage().contains("transport"));
        assertNull(result.transmissionCode());
        assertNull(result.receipt());
        assertNull(result.authorizationResult());
    }

    @Test
    void shouldReturnErrorOnMalformedResponse() {
        try (LocalSefazServer server = new LocalSefazServer("this is not xml")) {
            SefazTransmissionResult result = transmit(server);

            assertEquals(SefazTransmissionStatus.ERROR, result.transmissionStatus());
            assertEquals("Malformed SEFAZ response", result.transmissionMessage());
            assertNull(result.authorizationResult());
        }
    }

    @Test
    void shouldReturnErrorWhenCStatIsMissing() {
        try (LocalSefazServer server = new LocalSefazServer(MISSING_CSTAT_RESPONSE)) {
            SefazTransmissionResult result = transmit(server);

            assertEquals(SefazTransmissionStatus.ERROR, result.transmissionStatus());
            assertTrue(result.transmissionMessage().contains("cStat"));
        }
    }

    @Test
    void shouldRejectResponsesContainingExternalEntities() {
        try (LocalSefazServer server = new LocalSefazServer(XXE_RESPONSE)) {
            SefazTransmissionResult result = transmit(server);

            assertEquals(SefazTransmissionStatus.ERROR, result.transmissionStatus());
            assertEquals("Malformed SEFAZ response", result.transmissionMessage());
        }
    }

    @Test
    void shouldReturnErrorForBlankSignedXml() {
        try (LocalSefazServer server = new LocalSefazServer(AUTHORIZED_RESPONSE)) {
            NfeSefazTransmitter transmitter =
                    new NfeSefazTransmitter(properties("127.0.0.1", server.port()));

            SefazTransmissionResult result = transmitter.transmit("   ");

            assertEquals(SefazTransmissionStatus.ERROR, result.transmissionStatus());
            assertNull(server.lastRequest(), "no HTTP request may be sent for blank input");
        }
    }

    @Test
    void shouldNotExposeSensitiveMaterialInErrorMessages() {
        int refusedPort = refusedPort();
        NfeSefazTransmitter transmitter =
                new NfeSefazTransmitter(properties("127.0.0.1", refusedPort));

        SefazTransmissionResult result = transmitter.transmit(SIGNED_NFE);

        assertFalse(result.transmissionMessage().contains("<NFe"));
        assertFalse(result.transmissionMessage().contains("35260912345678"));
        assertFalse(result.transmissionMessage().contains("SignatureValue"));
        assertFalse(result.transmissionMessage().contains("password"));
    }

    private static SefazTransmissionResult transmit(LocalSefazServer server) {
        return transmit(server, SIGNED_NFE);
    }

    private static SefazTransmissionResult transmit(LocalSefazServer server, String signedXml) {
        NfeSefazTransmitter transmitter =
                new NfeSefazTransmitter(properties("127.0.0.1", server.port()));
        return transmitter.transmit(signedXml);
    }

    private static NfeSefazProperties properties(String host, int port) {
        return new NfeSefazProperties(false, "SP", NfeSefazEnvironment.HOMOLOGATION,
                "http://" + host + ":" + port + "/ws",
                Duration.ofSeconds(5), Duration.ofSeconds(10));
    }

    /**
     * Reserves and immediately releases a local port, so the following
     * connection attempt is refused — a deterministic transport failure.
     */
    private static int refusedPort() {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final class LocalSefazServer implements AutoCloseable {

        private final HttpServer server;
        private final AtomicReference<String> lastRequest = new AtomicReference<>();
        private final AtomicReference<Headers> lastHeaders = new AtomicReference<>();
        private final AtomicReference<String> lastMethod = new AtomicReference<>();
        private final AtomicReference<String> lastPath = new AtomicReference<>();

        LocalSefazServer(String responseBody) {
            try {
                server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            server.createContext("/ws", exchange -> {
                try {
                    byte[] body = exchange.getRequestBody().readAllBytes();
                    lastRequest.set(new String(body, StandardCharsets.UTF_8));
                    lastHeaders.set(exchange.getRequestHeaders());
                    lastMethod.set(exchange.getRequestMethod());
                    lastPath.set(exchange.getRequestURI().getPath());
                    byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/soap+xml; charset=utf-8");
                    exchange.sendResponseHeaders(200, response.length);
                    exchange.getResponseBody().write(response);
                } finally {
                    exchange.close();
                }
            });
            server.start();
        }

        int port() {
            return server.getAddress().getPort();
        }

        String lastRequest() {
            return lastRequest.get();
        }

        Headers lastHeaders() {
            return lastHeaders.get();
        }

        String lastMethod() {
            return lastMethod.get();
        }

        String lastPath() {
            return lastPath.get();
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}
