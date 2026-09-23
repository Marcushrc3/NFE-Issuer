package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class TaxTotalsTest {

    @Test
    void shouldAllowConstructionWithAllNullFields() {
        TaxTotals totals = new TaxTotals(null, null, null, null, null, null, null, null, null);

        assertNull(totals.icms());
        assertNull(totals.importTaxAmount());
        assertNull(totals.ipiAmount());
        assertNull(totals.ipiDevolvedAmount());
        assertNull(totals.pisAmount());
        assertNull(totals.cofinsAmount());
        assertNull(totals.totalTaxValue());
        assertNull(totals.is());
        assertNull(totals.ibsCbs());
    }

    @Test
    void shouldPreserveFlatValuesExactlyAsSupplied() {
        TaxTotals totals = new TaxTotals(
                null,
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                new BigDecimal("1.00"),
                new BigDecimal("1.65"),
                new BigDecimal("7.60"),
                new BigDecimal("40.25"),
                null,
                null);

        assertEquals(new BigDecimal("10.00"), totals.importTaxAmount());
        assertEquals(new BigDecimal("20.00"), totals.ipiAmount());
        assertEquals(new BigDecimal("1.00"), totals.ipiDevolvedAmount());
        assertEquals(new BigDecimal("1.65"), totals.pisAmount());
        assertEquals(new BigDecimal("7.60"), totals.cofinsAmount());
        assertEquals(new BigDecimal("40.25"), totals.totalTaxValue());
    }

    @Test
    void shouldNotDeriveTotalTaxValueFromOtherFields() {
        // supplied totalTaxValue (999) intentionally differs from
        // 10 + 20 + 1.65 + 7.60 = 39.25 — the domain must not recalculate.
        TaxTotals totals = new TaxTotals(
                null,
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                null,
                new BigDecimal("1.65"),
                new BigDecimal("7.60"),
                new BigDecimal("999"),
                null,
                null);

        assertEquals(new BigDecimal("999"), totals.totalTaxValue());
    }

    @Test
    void shouldPreserveNestedObjects() {
        IcmsTotals icms = new IcmsTotals(
                new BigDecimal("100.00"), new BigDecimal("18.00"), null, null,
                null, null, null, null, null, null, null);
        IsTotals is = new IsTotals(new BigDecimal("5.00"));
        IbsCbsTotals ibsCbs = new IbsCbsTotals(null, null, null, null, null);

        TaxTotals totals = new TaxTotals(
                icms, null, null, null, null, null, null, is, ibsCbs);

        assertSame(icms, totals.icms());
        assertSame(is, totals.is());
        assertSame(ibsCbs, totals.ibsCbs());
    }
}
