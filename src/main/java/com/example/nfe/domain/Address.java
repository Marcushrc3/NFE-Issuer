package com.example.nfe.domain;

/**
 * Address value object shared by {@link Issuer}, {@link Recipient} and
 * {@link Transport}.
 * <p>
 * {@code neighborhood} maps to {@code xBairro} and {@code municipalityCode}
 * to {@code cMun} (the 7-digit IBGE municipality code; the official schema
 * also accepts {@code 9999999} for operations abroad). Both are nullable
 * data fields with no defaults or derivation. {@code municipalityCode} only
 * gets the structural check required by the schema (7 digits) — no IBGE
 * lookup or business validation.
 */
public record Address(
        String street,
        String number,
        String city,
        String state,
        String zipCode,
        String neighborhood,
        String municipalityCode) {

    public Address {
        if (municipalityCode != null && !municipalityCode.matches("[0-9]{7}")) {
            throw new IllegalArgumentException(
                    "municipalityCode must be a 7-digit IBGE municipality code");
        }
    }
}
