package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * Imposto Seletivo (IS) of the RTC block, sibling of the IBSCBS group in the
 * item taxation.
 * <p>
 * Maps the verified official structure: {@code CSTIS}, {@code cClassTribIS},
 * {@code vBCIS}, {@code pIS}, {@code adRemIS}, {@code uTrib}, {@code qTrib}
 * and {@code vIS}. Every field may be null. Data only: no validation, no
 * calculations and no fiscal rules.
 */
public record IsTax(
        String cst,
        String cClassTrib,
        BigDecimal taxBase,
        BigDecimal rate,
        BigDecimal adRemRate,
        String taxableUnit,
        BigDecimal taxableQuantity,
        BigDecimal amount) {
}
