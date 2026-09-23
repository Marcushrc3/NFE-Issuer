package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class IcmsTaxTest {

    @Test
    void shouldExposeExistingCoreFields() {
        IcmsTax icms = new IcmsTax(
                "00", "3",
                new BigDecimal("100.00"), new BigDecimal("18.00"), new BigDecimal("18.00"),
                null, null, null, null, null);

        assertEquals("00", icms.cst());
        assertEquals("3", icms.modBc());
        assertEquals(new BigDecimal("100.00"), icms.taxBase());
        assertEquals(new BigDecimal("18.00"), icms.taxRate());
        assertEquals(new BigDecimal("18.00"), icms.taxAmount());
    }

    @Test
    void shouldAllowNewFieldsToBeNull() {
        IcmsTax icms = new IcmsTax(
                "00", "3",
                new BigDecimal("100.00"), new BigDecimal("18.00"), new BigDecimal("18.00"),
                null, null, null, null, null);

        assertNull(icms.fcpRate());
        assertNull(icms.fcpAmount());
        assertNull(icms.st());
        assertNull(icms.desonerationAmount());
        assertNull(icms.desonerationReason());
    }

    @Test
    void shouldAllowConstructionWithAllNullValues() {
        IcmsTax icms = new IcmsTax(null, null, null, null, null, null, null, null, null, null);

        assertNull(icms.cst());
        assertNull(icms.modBc());
        assertNull(icms.taxBase());
        assertNull(icms.taxRate());
        assertNull(icms.taxAmount());
        assertNull(icms.fcpRate());
        assertNull(icms.fcpAmount());
        assertNull(icms.st());
        assertNull(icms.desonerationAmount());
        assertNull(icms.desonerationReason());
    }

    @Test
    void shouldPreserveNewFieldsExactlyAsSupplied() {
        IcmsTax icms = new IcmsTax(
                "20", "0",
                new BigDecimal("80.00"), new BigDecimal("18.00"), new BigDecimal("14.40"),
                new BigDecimal("2.00"), new BigDecimal("1.60"),
                null,
                new BigDecimal("14.40"), "9");

        assertEquals("20", icms.cst());
        assertEquals("0", icms.modBc());
        assertEquals(new BigDecimal("2.00"), icms.fcpRate());
        assertEquals(new BigDecimal("1.60"), icms.fcpAmount());
        assertEquals(new BigDecimal("14.40"), icms.desonerationAmount());
        assertEquals("9", icms.desonerationReason());
    }

    @Test
    void shouldAllowNullSt() {
        IcmsTax icms = new IcmsTax(
                "00", "3",
                new BigDecimal("100.00"), new BigDecimal("18.00"), new BigDecimal("18.00"),
                null, null, null, null, null);

        assertNull(icms.st());
    }

    @Test
    void shouldPreserveSuppliedStObject() {
        IcmsStTax st = new IcmsStTax(
                "4", new BigDecimal("40.00"), new BigDecimal("10.00"),
                new BigDecimal("126.00"), new BigDecimal("18.00"), new BigDecimal("22.68"),
                new BigDecimal("126.00"), new BigDecimal("2.00"), new BigDecimal("2.52"));

        IcmsTax icms = new IcmsTax(
                "10", "3",
                new BigDecimal("100.00"), new BigDecimal("18.00"), new BigDecimal("18.00"),
                null, null, st, null, null);

        assertSame(st, icms.st());
    }
}
