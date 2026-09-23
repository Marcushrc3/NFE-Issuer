package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IbsJurisdictionTotalsTest {

    @Test
    void shouldAllowConstructionWithAllNullFields() {
        IbsJurisdictionTotals totals = new IbsJurisdictionTotals(null, null, null);

        assertNull(totals.dif());
        assertNull(totals.devTrib());
        assertNull(totals.amount());
    }

    @Test
    void shouldPreserveValuesExactlyAsSupplied() {
        IbsJurisdictionTotals totals = new IbsJurisdictionTotals(
                new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("17.50"));

        assertEquals(new BigDecimal("10.00"), totals.dif());
        assertEquals(new BigDecimal("2.00"), totals.devTrib());
        assertEquals(new BigDecimal("17.50"), totals.amount());
    }
}
