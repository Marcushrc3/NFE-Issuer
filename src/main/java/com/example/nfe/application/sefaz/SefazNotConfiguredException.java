package com.example.nfe.application.sefaz;

/**
 * Thrown when the EMIT processing mode requires SEFAZ transmission but no
 * {@link SefazTransmitter} is configured. A controlled application error —
 * the application NEVER pretends a transmission happened and NEVER returns
 * an AUTHORIZED status or a fabricated protocol.
 */
public class SefazNotConfiguredException extends RuntimeException {

    public SefazNotConfiguredException(String message) {
        super(message);
    }
}
