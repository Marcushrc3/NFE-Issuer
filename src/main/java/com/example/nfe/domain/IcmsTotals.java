package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * Emission-level ICMS totals, mirroring the {@code ICMSTot} group.
 * <p>
 * Maps {@code vBC}, {@code vICMS}, {@code vICMSDeson}, {@code vFCP},
 * {@code vFCPUFDest}, {@code vICMSUFDest}, {@code vICMSUFRemet},
 * {@code vBCST}, {@code vST}, {@code vFCPST} and {@code vFCPSTRet}. The
 * DIFAL fields (destination/sender) are structural holders only — no DIFAL
 * rules are applied. All fields may be null. Data only: no validation, no
 * calculations and no fiscal rules.
 */
public record IcmsTotals(
        BigDecimal base,
        BigDecimal amount,
        BigDecimal desonerationAmount,
        BigDecimal fcp,
        BigDecimal fcpUfDestination,
        BigDecimal ufDestination,
        BigDecimal ufSender,
        BigDecimal stBase,
        BigDecimal stAmount,
        BigDecimal fcpStAmount,
        BigDecimal fcpStRetained) {
}
