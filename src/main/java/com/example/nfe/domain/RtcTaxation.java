package com.example.nfe.domain;

/**
 * Core structure of the {@code IBSCBS} group of the RTC block.
 * <p>
 * Maps {@code CST}, {@code cClassTrib}, {@code indDoacao} and the
 * {@code gIBSCBS} group (via {@link IbsCbsTaxation}). Codes are plain
 * strings; no enums. {@code indDoacao} may be null. The Imposto Seletivo
 * ({@code IS}) is a sibling of IBSCBS and is not part of this record. Data
 * only: no validation, no calculations and no fiscal rules.
 */
public record RtcTaxation(
        String cst,
        String cClassTrib,
        String indDoacao,
        IbsCbsTaxation ibsCbs) {
}
