package com.example.nfe.application.sefaz;

/**
 * Application-level port for SEFAZ transmission.
 * <p>
 * Receives the exact signed NF-e XML produced by the signing step and
 * returns a structured transmission result. The interface is independent
 * from Spring, JAXB, SOAP, HTTP and any specific SEFAZ WebService.
 * <p>
 * The real SOAP/HTTP implementation lives in
 * {@code infrastructure.sefaz.nfe.NfeSefazTransmitter} and is registered
 * only when {@code nfe.sefaz.enabled=true}; without it, EMIT fails with a
 * controlled {@code SefazNotConfiguredException}.
 */
public interface SefazTransmitter {

    /**
     * Transmits the signed NF-e document to SEFAZ and returns the
     * transmission result.
     *
     * @param signedXml the exact signed XML produced by the signing step
     * @return the structured transmission result
     */
    SefazTransmissionResult transmit(String signedXml);
}
