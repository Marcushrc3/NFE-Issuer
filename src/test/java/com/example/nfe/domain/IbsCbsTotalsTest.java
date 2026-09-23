package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class IbsCbsTotalsTest {

    @Test
    void shouldAllowConstructionWithAllNullFields() {
        IbsCbsTotals totals = new IbsCbsTotals(null, null, null, null, null);

        assertNull(totals.base());
        assertNull(totals.uf());
        assertNull(totals.municipality());
        assertNull(totals.amount());
        assertNull(totals.cbs());
    }

    @Test
    void shouldPreserveValuesExactlyAsSupplied() {
        IbsJurisdictionTotals uf = new IbsJurisdictionTotals(
                new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("17.50"));
        IbsJurisdictionTotals municipality = new IbsJurisdictionTotals(
                new BigDecimal("5.00"), new BigDecimal("1.00"), new BigDecimal("9.00"));
        CbsTotals cbs = new CbsTotals(
                new BigDecimal("5.00"), new BigDecimal("1.00"), new BigDecimal("8.80"));

        IbsCbsTotals totals = new IbsCbsTotals(
                new BigDecimal("100.00"), uf, municipality, new BigDecimal("26.50"), cbs);

        assertEquals(new BigDecimal("100.00"), totals.base());
        assertSame(uf, totals.uf());
        assertSame(municipality, totals.municipality());
        assertEquals(new BigDecimal("26.50"), totals.amount());
        assertSame(cbs, totals.cbs());
    }

    @Test
    void shouldKeepIbsTotalIndependentFromJurisdictionAmounts() {
        // supplied amount (30.00) intentionally differs from
        // 17.50 + 9.00 = 26.50 — the domain must not recalculate.
        IbsCbsTotals totals = new IbsCbsTotals(
                new BigDecimal("100.00"),
                new IbsJurisdictionTotals(null, null, new BigDecimal("17.50")),
                new IbsJurisdictionTotals(null, null, new BigDecimal("9.00")),
                new BigDecimal("30.00"),
                null);

        assertEquals(new BigDecimal("30.00"), totals.amount());
    }
}
