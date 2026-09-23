package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * IPI data holder. No tax rules, validations or calculations are applied by
 * the domain.
 * <p>
 * Deferred until fiscal rules are established: legal framework code (cEnq) and
 * exemption details.
 */
public record IpiTax(
        String cst,
        BigDecimal taxBase,
        BigDecimal taxRate,
        BigDecimal taxAmount) {
}
