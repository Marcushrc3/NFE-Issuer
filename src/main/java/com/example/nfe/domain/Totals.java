package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * Monetary summary of the emission.
 * <p>
 * All fields are supplied explicitly — the domain performs no calculation and
 * no relationship between {@code itemsTotal}, {@code freight},
 * {@code insurance}, {@code discount}, {@code otherCharges} and
 * {@code totalValue} is enforced. The optional monetary fields may be null.
 * {@code totalValue} is a placeholder for the final NF total.
 */
public record Totals(
        BigDecimal itemsTotal,
        BigDecimal freight,
        BigDecimal insurance,
        BigDecimal discount,
        BigDecimal otherCharges,
        BigDecimal totalValue) {
}
