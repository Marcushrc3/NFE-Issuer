package com.example.nfe.application.sefaz;

/**
 * Outcome of the NF-e authorization itself, as reported by SEFAZ inside a
 * batch response ({@code protNFe/infProt}).
 * <p>
 * Distinct from {@link SefazTransmissionStatus}, which describes the whole
 * transmission. An authorization result only exists when SEFAZ actually
 * emitted an authorization or denial protocol for the document.
 */
public enum SefazAuthorizationStatus {

    /**
     * Official {@code infProt.cStat = 100} — "Autorizado o uso da NF-e".
     */
    AUTHORIZED,

    /**
     * Official denial/rejection outcome (e.g. {@code infProt.cStat = 110} —
     * "Uso Denegado") or any other non-authorization protocol status.
     */
    REJECTED
}
