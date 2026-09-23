package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CbsTotalsTest {

    @Test
    void shouldAllowConstructionWithAllNullFields() {
        CbsTotals totals = new CbsTotals(null, null, null);

        assertNull(totals.dif());
        assertNull(totals.devTrib());
        assertNull(totals.amount());
    }

    @Test
    void shouldPreserveValuesExactlyAsSupplied() {
        CbsTotals totals = new CbsTotals(
                new BigDecimal("5.00"), new BigDecimal("1.00"), new BigDecimal("8.80"));

        assertEquals(new BigDecimal("5.00"), totals.dif());
        assertEquals(new BigDecimal("1.00"), totals.devTrib());
        assertEquals(new BigDecimal("8.80"), totals.amount());
    }
}
