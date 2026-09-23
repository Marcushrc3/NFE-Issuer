package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * Emission-level CBS totals, mirroring the {@code gCBS} total group.
 * <p>
 * Maps {@code vDif} to {@code dif}, {@code vDevTrib} to {@code devTrib} and
 * {@code vCBS} to {@code amount}. All fields may be null. Data only: no
 * validation, no calculations and no fiscal rules.
 */
public record CbsTotals(
        BigDecimal dif,
        BigDecimal devTrib,
        BigDecimal amount) {
}
