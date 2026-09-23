package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentTest {

    @Test
    void shouldPreservePaymentDetailFields() {
        PaymentDetail detail = new PaymentDetail("17",
                new BigDecimal("9.99"), PaymentIndicator.IMMEDIATE, LocalDate.of(2026, 9, 21));

        assertEquals("17", detail.paymentMethod());
        assertEquals(new BigDecimal("9.99"), detail.amount());
        assertEquals(PaymentIndicator.IMMEDIATE, detail.indicator());
        assertEquals(LocalDate.of(2026, 9, 21), detail.paymentDate());
    }

    @Test
    void shouldAllowNullOptionalDetailFields() {
        PaymentDetail detail = new PaymentDetail("01", new BigDecimal("1.00"), null, null);

        assertNull(detail.indicator());
        assertNull(detail.paymentDate());
    }

    @Test
    void shouldPreserveMultiplePaymentDetails() {
        Payment payment = new Payment(List.of(
                new PaymentDetail("17", new BigDecimal("9.00"), null, null),
                new PaymentDetail("01", new BigDecimal("1.00"), null, null)));

        assertEquals(2, payment.details().size());
        assertEquals("17", payment.details().get(0).paymentMethod());
        assertEquals("01", payment.details().get(1).paymentMethod());
    }

    @Test
    void shouldRejectEmptyPayment() {
        assertThrows(IllegalArgumentException.class, () -> new Payment(List.of()));
    }
}
