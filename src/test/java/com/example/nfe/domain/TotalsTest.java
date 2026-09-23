package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TotalsTest {

    @Test
    void shouldAllowConstructionWithAllNullMonetaryFields() {
        Totals totals = new Totals(null, null, null, null, null, null);

        assertNull(totals.itemsTotal());
        assertNull(totals.freight());
        assertNull(totals.insurance());
        assertNull(totals.discount());
        assertNull(totals.otherCharges());
        assertNull(totals.totalValue());
    }

    @Test
    void shouldAllowNullOptionalMonetaryFields() {
        Totals totals = new Totals(
                new BigDecimal("1000"), null, null, null, null, new BigDecimal("1000"));

        assertEquals(new BigDecimal("1000"), totals.itemsTotal());
        assertNull(totals.freight());
        assertNull(totals.insurance());
        assertNull(totals.discount());
        assertNull(totals.otherCharges());
        assertEquals(new BigDecimal("1000"), totals.totalValue());
    }

    @Test
    void shouldPreserveMonetaryValuesExactlyAsSupplied() {
        Totals totals = new Totals(
                new BigDecimal("1000"),
                new BigDecimal("100"),
                new BigDecimal("20"),
                new BigDecimal("10"),
                new BigDecimal("5"),
                new BigDecimal("1115"));

        assertEquals(new BigDecimal("1000"), totals.itemsTotal());
        assertEquals(new BigDecimal("100"), totals.freight());
        assertEquals(new BigDecimal("20"), totals.insurance());
        assertEquals(new BigDecimal("10"), totals.discount());
        assertEquals(new BigDecimal("5"), totals.otherCharges());
        assertEquals(new BigDecimal("1115"), totals.totalValue());
    }

    @Test
    void shouldNotCalculateTotalValueFromComposition() {
        // supplied totalValue (2000) intentionally differs from
        // 1000 + 100 + 20 - 10 + 5 = 1115 — the domain must not recalculate.
        Totals totals = new Totals(
                new BigDecimal("1000"),
                new BigDecimal("100"),
                new BigDecimal("20"),
                new BigDecimal("10"),
                new BigDecimal("5"),
                new BigDecimal("2000"));

        assertEquals(new BigDecimal("2000"), totals.totalValue());
    }
}
