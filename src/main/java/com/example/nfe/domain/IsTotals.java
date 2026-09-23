package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * Emission-level Imposto Seletivo totals, mirroring the {@code ISTot} group.
 * <p>
 * Maps {@code vIS} to {@code amount}. May be null. Data only: no validation,
 * no calculations and no fiscal rules.
 */
public record IsTotals(
        BigDecimal amount) {
}
