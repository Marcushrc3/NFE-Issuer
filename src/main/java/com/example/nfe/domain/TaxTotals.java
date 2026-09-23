package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * Emission-level tax totals, mirroring the groups of the NF-e {@code total}
 * block.
 * <p>
 * Maps {@code vII}, {@code vIPI}, {@code vIPIDevol}, {@code vPIS},
 * {@code vCOFINS} and {@code vTotTrib} to the flat fields, with the
 * {@code ICMSTot}, {@code ISTot} and {@code IBSCBSTot} schema groups
 * represented by nested value objects. All fields may be null and
 * {@code totalTaxValue} is never derived from the other fields. Data only:
 * no validation, no calculations and no fiscal rules.
 */
public record TaxTotals(
        IcmsTotals icms,
        BigDecimal importTaxAmount,
        BigDecimal ipiAmount,
        BigDecimal ipiDevolvedAmount,
        BigDecimal pisAmount,
        BigDecimal cofinsAmount,
        BigDecimal totalTaxValue,
        IsTotals is,
        IbsCbsTotals ibsCbs) {
}
