package com.example.nfe.application.service;

/**
 * Thrown when the EMIT processing mode is requested but no signing
 * credentials/XmlSigner are configured. A controlled application error —
 * never a NullPointerException. XML_ONLY never triggers it.
 */
public class SigningNotConfiguredException extends RuntimeException {

    public SigningNotConfiguredException(String message) {
        super(message);
    }
}
