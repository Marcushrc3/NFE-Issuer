package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * Conditional {@code gRed} group of the RTC block (redução de alíquota).
 * <p>
 * Maps {@code pRedAliq} to {@code reductionRate} and {@code pAliqEfet} to
 * {@code effectiveRate}. Data only: no validation, no calculations and no
 * fiscal rules.
 */
public record RtcReduction(
        BigDecimal reductionRate,
        BigDecimal effectiveRate) {
}
