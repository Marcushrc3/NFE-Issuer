package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * Imposto de Importação (II) data holder, relevant only for imported items.
 * No tax rules, validations or calculations are applied by the domain.
 * <p>
 * {@code customsExpenses} (despesas aduaneiras) and {@code iofAmount} (IOF)
 * are structural companions of the II values and may be null.
 */
public record ImportTax(
        BigDecimal taxBase,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal customsExpenses,
        BigDecimal iofAmount) {
}
