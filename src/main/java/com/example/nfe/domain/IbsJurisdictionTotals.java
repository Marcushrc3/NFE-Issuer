package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * Emission-level IBS totals for one jurisdiction (UF or municipality),
 * mirroring the {@code gIBSUF}/{@code gIBSMun} total groups.
 * <p>
 * Maps {@code vDif} to {@code dif}, {@code vDevTrib} to {@code devTrib} and
 * {@code vIBSUF}/{@code vIBSMun} to {@code amount}; the parent context
 * determines the jurisdiction. All fields may be null. Data only: no
 * validation, no calculations and no fiscal rules.
 */
public record IbsJurisdictionTotals(
        BigDecimal dif,
        BigDecimal devTrib,
        BigDecimal amount) {
}
