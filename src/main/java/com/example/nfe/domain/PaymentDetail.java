package com.example.nfe.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A single payment form detail (detPag).
 * <p>
 * {@code paymentMethod} is the official 2-digit payment form code
 * (e.g. 01 dinheiro, 17 PIX) kept as plain data — the PL_010f schema only
 * enforces the two-digit pattern, so no domain enum is imposed.
 * {@code amount} is the explicit payment value (vPag) and is never derived
 * from totals or other payments. {@code indicator} (indPag) and
 * {@code paymentDate} (dPag) are optional business data.
 */
public record PaymentDetail(
        String paymentMethod,
        BigDecimal amount,
        PaymentIndicator indicator,
        LocalDate paymentDate) {
}
