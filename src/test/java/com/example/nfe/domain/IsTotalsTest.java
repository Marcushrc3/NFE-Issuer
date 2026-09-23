package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IsTotalsTest {

    @Test
    void shouldAllowNullAmount() {
        IsTotals totals = new IsTotals(null);

        assertNull(totals.amount());
    }

    @Test
    void shouldPreserveAmountExactlyAsSupplied() {
        IsTotals totals = new IsTotals(new BigDecimal("5.00"));

        assertEquals(new BigDecimal("5.00"), totals.amount());
    }
}
