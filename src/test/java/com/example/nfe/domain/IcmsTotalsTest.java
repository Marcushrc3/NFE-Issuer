package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IcmsTotalsTest {

    @Test
    void shouldAllowConstructionWithAllNullFields() {
        IcmsTotals totals = new IcmsTotals(null, null, null, null, null, null, null, null, null, null, null);

        assertNull(totals.base());
        assertNull(totals.amount());
        assertNull(totals.desonerationAmount());
        assertNull(totals.fcp());
        assertNull(totals.fcpUfDestination());
        assertNull(totals.ufDestination());
        assertNull(totals.ufSender());
        assertNull(totals.stBase());
        assertNull(totals.stAmount());
        assertNull(totals.fcpStAmount());
        assertNull(totals.fcpStRetained());
    }

    @Test
    void shouldPreserveValuesExactlyAsSupplied() {
        IcmsTotals totals = new IcmsTotals(
                new BigDecimal("100.00"),
                new BigDecimal("18.00"),
                new BigDecimal("1.00"),
                new BigDecimal("2.00"),
                new BigDecimal("1.50"),
                new BigDecimal("10.00"),
                new BigDecimal("8.00"),
                new BigDecimal("50.00"),
                new BigDecimal("9.00"),
                new BigDecimal("1.00"),
                new BigDecimal("0.50"));

        assertEquals(new BigDecimal("100.00"), totals.base());
        assertEquals(new BigDecimal("18.00"), totals.amount());
        assertEquals(new BigDecimal("1.00"), totals.desonerationAmount());
        assertEquals(new BigDecimal("2.00"), totals.fcp());
        assertEquals(new BigDecimal("1.50"), totals.fcpUfDestination());
        assertEquals(new BigDecimal("10.00"), totals.ufDestination());
        assertEquals(new BigDecimal("8.00"), totals.ufSender());
        assertEquals(new BigDecimal("50.00"), totals.stBase());
        assertEquals(new BigDecimal("9.00"), totals.stAmount());
        assertEquals(new BigDecimal("1.00"), totals.fcpStAmount());
        assertEquals(new BigDecimal("0.50"), totals.fcpStRetained());
    }
}
