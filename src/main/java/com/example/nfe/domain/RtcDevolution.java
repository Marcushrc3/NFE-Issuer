package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * Conditional {@code gDevTrib} group of the RTC block (devolução de tributos).
 * <p>
 * Maps {@code pDevTrib} to {@code rate} (optional in the layout, may be null)
 * and {@code vDevTrib} to {@code amount}. Data only: no validation, no
 * calculations and no fiscal rules.
 */
public record RtcDevolution(
        BigDecimal rate,
        BigDecimal amount) {
}
