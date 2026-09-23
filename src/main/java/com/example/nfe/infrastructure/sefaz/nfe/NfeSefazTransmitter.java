package com.example.nfe.infrastructure.sefaz.nfe;

import com.example.nfe.application.sefaz.SefazAuthorizationResult;
import com.example.nfe.application.sefaz.SefazAuthorizationStatus;
import com.example.nfe.application.sefaz.SefazTransmissionResult;
import com.example.nfe.application.sefaz.SefazTransmissionStatus;
import com.example.nfe.application.sefaz.SefazTransmitter;
import com.example.nfe.infrastructure.sefaz.config.NfeSefazEnvironment;
import com.example.nfe.infrastructure.sefaz.config.NfeSefazProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * SEFAZ authorization adapter: sends the already signed NF-e to the official
 * {@code NFeAutorizacao4} WebService (UF SP, layout 4.00) behind the
 * application-level {@link SefazTransmitter} port.
 * <p>
 * Transport: standard JDK {@link HttpClient} over HTTPS with the system
 * trust store — TLS certificate validation and hostname verification stay
 * enabled (TLS 1.2). No client certificate is used here: the A1 certificate
 * belongs to the XML signing step and is never read, logged or stored by
 * this adapter.
 * <p>
 * The signed document received by {@link #transmit(String)} is embedded in
 * the batch envelope verbatim — it is never regenerated, re-signed or
 * modified (only its XML declaration is stripped for embedding).
 * <p>
 * Status mapping (official cStat semantics from the MOC — Manual de
 * Orientação ao Contribuinte). The batch level (retEnviNFe) and the
 * authorization level (protNFe/infProt) are preserved separately:
 * <ul>
 *   <li>batch 103 + infProt cStat 100 → AUTHORIZED + authorization result</li>
 *   <li>batch 103 + infProt other cStat (e.g. 110) → REJECTED + result</li>
 *   <li>batch 103 with infRec/nRec, without protocol → PROCESSING (receipt)</li>
 *   <li>batch 104 with protocol → authorization result when present,
 *       otherwise PROCESSING</li>
 *   <li>batch 105 → PROCESSING</li>
 *   <li>any other batch cStat (225, 539, 999, ...) → REJECTED, no
 *       authorization result</li>
 *   <li>transport failure / malformed response → ERROR</li>
 * </ul>
 * No status is ever invented: rejected or failed transmissions never carry
 * an authorization protocol.
 */
public class NfeSefazTransmitter implements SefazTransmitter {

    private static final Logger LOGGER = LoggerFactory.getLogger(NfeSefazTransmitter.class);

    private static final String CONTENT_TYPE = "application/soap+xml; charset=utf-8";

    private final NfeSefazProperties properties;
    private final HttpClient httpClient;

    public NfeSefazTransmitter(NfeSefazProperties properties) {
        this(properties, buildHttpClient(properties));
    }

    NfeSefazTransmitter(NfeSefazProperties properties, HttpClient httpClient) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
    }

    private static HttpClient buildHttpClient(NfeSefazProperties properties) {
        SSLParameters sslParameters = new SSLParameters();
        sslParameters.setProtocols(new String[]{"TLSv1.2"});
        return HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .sslParameters(sslParameters)
                .build();
    }

    @Override
    public SefazTransmissionResult transmit(String signedXml) {
        if (signedXml == null || signedXml.isBlank()) {
            return error("Signed NF-e XML must not be blank", null);
        }
        warnOnEnvironmentMismatch();

        String envelope = NfeAuthorizationSoapEnvelope.build(signedXml, generateIdLote());
        HttpRequest request = HttpRequest.newBuilder(URI.create(properties.endpoint()))
                .timeout(properties.readTimeout())
                .header("Content-Type", CONTENT_TYPE)
                .header("SOAPAction", NfeAuthorizationSoapEnvelope.SOAP_ACTION)
                .POST(HttpRequest.BodyPublishers.ofString(envelope, StandardCharsets.UTF_8))
                .build();

        String rawResponse;
        int statusCode;
        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            statusCode = response.statusCode();
            rawResponse = response.body();
        } catch (IOException e) {
            // Network-level failures must never leak request content (the
            // signed document) — only the exception type is reported.
            LOGGER.warn("SEFAZ transport failure: {}", e.getClass().getSimpleName());
            return error("SEFAZ transport failure: " + e.getClass().getSimpleName(), null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return error("SEFAZ transmission interrupted", null);
        }

        NfeAuthorizationResponseParser.Parsed parsed;
        try {
            parsed = NfeAuthorizationResponseParser.parse(rawResponse);
        } catch (Exception e) {
            LOGGER.warn("SEFAZ response could not be parsed ({})", e.getClass().getSimpleName());
            return error("Malformed SEFAZ response", rawResponse);
        }
        if (statusCode >= 400) {
            return error("SEFAZ HTTP error: " + statusCode, rawResponse);
        }
        return toResult(parsed, rawResponse);
    }

    private SefazTransmissionResult toResult(
            NfeAuthorizationResponseParser.Parsed parsed, String rawResponse) {

        Integer cStat = parsed.cStat();
        if (cStat == null) {
            LOGGER.warn("SEFAZ response is missing cStat");
            return error("SEFAZ response is missing cStat", rawResponse);
        }
        return switch (cStat) {
            case 103 -> {
                if (parsed.nProt() != null) {
                    if (parsed.protCStat() == null) {
                        yield error("SEFAZ response is missing the protocol status (infProt/cStat)", rawResponse);
                    }
                    SefazAuthorizationResult authorization = toAuthorizationResult(parsed);
                    yield new SefazTransmissionResult(
                            authorization.status() == SefazAuthorizationStatus.AUTHORIZED
                                    ? SefazTransmissionStatus.AUTHORIZED
                                    : SefazTransmissionStatus.REJECTED,
                            103,
                            parsed.xMotivo(),
                            null,
                            authorization,
                            rawResponse);
                }
                if (parsed.nRec() != null) {
                    yield new SefazTransmissionResult(
                            SefazTransmissionStatus.PROCESSING,
                            103,
                            parsed.xMotivo(),
                            parsed.nRec(),
                            null,
                            rawResponse);
                }
                yield error("SEFAZ response contains neither protocol nor receipt", rawResponse);
            }
            case 104 -> {
                // Batch processed: the authorization outcome (if any) comes
                // from the protocol — never invented from the batch code.
                if (parsed.nProt() != null && parsed.protCStat() != null) {
                    SefazAuthorizationResult authorization = toAuthorizationResult(parsed);
                    yield new SefazTransmissionResult(
                            authorization.status() == SefazAuthorizationStatus.AUTHORIZED
                                    ? SefazTransmissionStatus.AUTHORIZED
                                    : SefazTransmissionStatus.REJECTED,
                            104,
                            parsed.xMotivo(),
                            parsed.nRec(),
                            authorization,
                            rawResponse);
                }
                yield new SefazTransmissionResult(
                        SefazTransmissionStatus.PROCESSING,
                        104,
                        parsed.xMotivo(),
                        parsed.nRec(),
                        null,
                        rawResponse);
            }
            case 105 -> new SefazTransmissionResult(
                    SefazTransmissionStatus.PROCESSING,
                    105,
                    parsed.xMotivo(),
                    parsed.nRec(),
                    null,
                    rawResponse);
            default -> new SefazTransmissionResult(
                    SefazTransmissionStatus.REJECTED,
                    cStat,
                    parsed.xMotivo(),
                    parsed.nRec(),
                    null,
                    rawResponse);
        };
    }

    /**
     * Builds the authorization result from the official protocol fields —
     * called only when {@code protNFe/infProt} actually exists.
     */
    private SefazAuthorizationResult toAuthorizationResult(
            NfeAuthorizationResponseParser.Parsed parsed) {
        if (parsed.protCStat() == 100) {
            return new SefazAuthorizationResult(
                    SefazAuthorizationStatus.AUTHORIZED,
                    100,
                    parsed.protXMotivo() != null ? parsed.protXMotivo() : "Autorizado o uso da NF-e",
                    parsed.nProt(),
                    parsed.chNFe(),
                    parsed.digVal());
        }
        return new SefazAuthorizationResult(
                SefazAuthorizationStatus.REJECTED,
                parsed.protCStat(),
                parsed.protXMotivo() != null ? parsed.protXMotivo() : "Uso não autorizado da NF-e",
                parsed.nProt(),
                parsed.chNFe(),
                parsed.digVal());
    }

    private SefazTransmissionResult error(String message, String rawResponse) {
        return new SefazTransmissionResult(
                SefazTransmissionStatus.ERROR, null, message, null, null, rawResponse);
    }

    /**
     * The batch identifier is transport metadata (1..15 digits), generated by
     * the emitting application — it is not part of the signed document.
     */
    private static String generateIdLote() {
        return String.format("%015d", System.currentTimeMillis() % 1_000_000_000_000_000L);
    }

    /**
     * Guard against the most dangerous configuration mistake: declaring the
     * PRODUCTION environment while still pointing at a homologation host.
     */
    private void warnOnEnvironmentMismatch() {
        if (properties.environment() == NfeSefazEnvironment.PRODUCTION
                && properties.endpoint().contains("homolog")) {
            LOGGER.warn("nfe.sefaz.environment=PRODUCTION but the endpoint looks like a homologation URL");
        }
    }
}
