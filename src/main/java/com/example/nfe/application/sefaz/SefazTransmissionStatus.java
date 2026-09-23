package com.example.nfe.application.sefaz;

/**
 * Outcome of a SEFAZ transmission attempt, as reported by a
 * {@link SefazTransmitter} implementation.
 */
public enum SefazTransmissionStatus {
    /** The document was authorized by SEFAZ. */
    AUTHORIZED,
    /** The document was rejected by SEFAZ. */
    REJECTED,
    /** The document is still being processed by SEFAZ. */
    PROCESSING,
    /** The transmission itself failed (transport/communication error). */
    ERROR
}
