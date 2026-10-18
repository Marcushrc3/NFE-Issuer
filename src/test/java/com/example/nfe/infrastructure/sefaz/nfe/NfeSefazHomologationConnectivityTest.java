package com.example.nfe.infrastructure.sefaz.nfe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Manual connectivity check against the official SEFAZ-SP homologation
 * endpoint (TLS only — never a SOAP POST, never a fiscal XML).
 *
 * <p>This test is NOT part of the regular suite: it depends on network
 * access and on a truststore that trusts the official ICP-Brasil chain.
 * It runs only when explicitly activated:
 *
 * <pre>
 * mvn test \
 *   -Dnfe.sefaz.connectivity=true \
 *   -Dtest=NfeSefazHomologationConnectivityTest \
 *   -Djavax.net.ssl.trustStore=C:\caminho\sefaz-truststore.p12 \
 *   -Djavax.net.ssl.trustStorePassword=SUA_SENHA
 * </pre>
 *
 * <p>Success means the TLS handshake completed with certificate validation
 * ENABLED and an HTTP response was received. The SEFAZ service answers a
 * GET with an HTTP error (403 observed) because it only accepts SOAP POST
 * — any HTTP status proves the handshake, never the fiscal authorization.
 * Failures are classified: DNS resolution, TCP connection, TLS handshake
 * and unexpected I/O.
 */
@EnabledIfSystemProperty(named = "nfe.sefaz.connectivity", matches = "true")
class NfeSefazHomologationConnectivityTest {

    private static final URI HOMOLOGATION_ENDPOINT = URI.create(
            "https://homologacao.nfe.fazenda.sp.gov.br/ws/nfeautorizacao4.asmx");

    @Test
    void shouldCompleteTlsHandshakeWithConfiguredTruststore() {
        try {
            HttpResponse<String> response = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(15))
                    .build()
                    .send(HttpRequest.newBuilder(HOMOLOGATION_ENDPOINT)
                                    .timeout(Duration.ofSeconds(30))
                                    .GET()
                                    .build(),
                            HttpResponse.BodyHandlers.ofString());
            assertTrue(response.statusCode() >= 200 && response.statusCode() <= 599,
                    "expected any HTTP response (handshake OK); got " + response.statusCode());
        } catch (UnknownHostException e) {
            fail("DNS failure — the homologation host did not resolve: " + e.getMessage());
        } catch (ConnectException e) {
            fail("connection failure — TCP connect did not complete: " + e.getMessage());
        } catch (SSLException e) {
            fail("TLS handshake failure — check javax.net.ssl.trustStore/trustStorePassword: "
                    + e.getMessage());
        } catch (IOException e) {
            fail("unexpected I/O failure after transport: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("connectivity check interrupted");
        }
    }
}
