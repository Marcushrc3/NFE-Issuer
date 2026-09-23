package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * Conditional {@code gDif} group of the RTC block (diferimento).
 * <p>
 * Maps {@code pDif} to {@code rate} and {@code vDif} to {@code amount}.
 * Data only: no validation, no calculations and no fiscal rules.
 */
public record RtcDeferral(
        BigDecimal rate,
        BigDecimal amount) {
}
