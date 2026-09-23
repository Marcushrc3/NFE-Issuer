package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * PIS/COFINS data holder. No tax rules, validations or calculations are
 * applied by the domain.
 * <p>
 * Deferred until fiscal rules are established: withholding and per-regime
 * details.
 */
public record PisCofinsTax(
        String pisCst,
        BigDecimal pisBase,
        BigDecimal pisRate,
        BigDecimal pisAmount,
        String cofinsCst,
        BigDecimal cofinsBase,
        BigDecimal cofinsRate,
        BigDecimal cofinsAmount) {
}
