package com.example.nfe.domain;

/**
 * Payment indicator (indPag), mirroring the official NF-e semantics:
 * <ul>
 *   <li>{@link #IMMEDIATE} — payment at sight (à vista)</li>
 *   <li>{@link #DEFERRED} — deferred payment (à prazo)</li>
 * </ul>
 * <p>
 * This enum carries business meaning only. The official numeric codes
 * (0, 1) belong to the XML infrastructure mapping layer.
 */
public enum PaymentIndicator {
    IMMEDIATE,
    DEFERRED
}
