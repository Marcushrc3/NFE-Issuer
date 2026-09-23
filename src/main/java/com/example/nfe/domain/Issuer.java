package com.example.nfe.domain;

/**
 * Issuer (emitente) of the NF-e.
 * <p>
 * {@code tradeName} maps to {@code xFant} (optional trade/fantasy name) and
 * {@code taxRegime} maps to {@code CRT} (Código de Regime Tributário, values
 * 1–4 per the official schema). Both are nullable data fields with no
 * validation, defaults or derivation. {@code stateRegistration} and
 * {@code address} are provisional fields and do not yet represent the
 * official NF-e model.
 */
public record Issuer(
        String name,
        String document,
        String stateRegistration,
        Address address,
        String tradeName,
        String taxRegime) {
}
