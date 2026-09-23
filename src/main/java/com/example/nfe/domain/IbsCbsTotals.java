package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * Emission-level IBS/CBS totals, mirroring the core of the
 * {@code IBSCBSTot} group.
 * <p>
 * Maps {@code vBCIBSCBS} to {@code base}, {@code gIBSUF} to {@code uf},
 * {@code gIBSMun} to {@code municipality}, {@code vIBS} to {@code amount}
 * and {@code gCBS} to {@code cbs}. The IBS value is explicit and never
 * derived from the jurisdiction amounts. Presumed credits, monophase and
 * credit-reversal totals remain deferred and are not modelled. All fields
 * may be null. Data only: no validation, no calculations and no fiscal
 * rules.
 */
public record IbsCbsTotals(
        BigDecimal base,
        IbsJurisdictionTotals uf,
        IbsJurisdictionTotals municipality,
        BigDecimal amount,
        CbsTotals cbs) {
}
