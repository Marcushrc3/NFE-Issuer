package com.example.nfe.application.service;

/**
 * Controlled application error for a SEFAZ transmission that ended with
 * {@code ERROR} (transport failure, HTTP error or malformed response).
 * <p>
 * Carries only the sanitized transmission message — never the signed NF-e,
 * credentials or raw SOAP content. Mapped to HTTP 500 by the global
 * exception handler.
 */
public class SefazTransmissionFailedException extends RuntimeException {

    public SefazTransmissionFailedException(String message) {
        super(message);
    }
}
