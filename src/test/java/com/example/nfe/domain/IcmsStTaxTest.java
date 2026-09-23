package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IcmsStTaxTest {

    @Test
    void shouldAllowConstructionWithAllNullValues() {
        IcmsStTax st = new IcmsStTax(null, null, null, null, null, null, null, null, null);

        assertNull(st.modBcSt());
        assertNull(st.stMarginRate());
        assertNull(st.stReductionRate());
        assertNull(st.stBase());
        assertNull(st.stRate());
        assertNull(st.stAmount());
        assertNull(st.fcpStBase());
        assertNull(st.fcpStRate());
        assertNull(st.fcpStAmount());
    }

    @Test
    void shouldAllowConstructionWithAllValues() {
        IcmsStTax st = new IcmsStTax(
                "4", new BigDecimal("40.00"), new BigDecimal("10.00"),
                new BigDecimal("126.00"), new BigDecimal("18.00"), new BigDecimal("22.68"),
                new BigDecimal("126.00"), new BigDecimal("2.00"), new BigDecimal("2.52"));

        assertEquals("4", st.modBcSt());
        assertEquals(new BigDecimal("40.00"), st.stMarginRate());
        assertEquals(new BigDecimal("10.00"), st.stReductionRate());
        assertEquals(new BigDecimal("126.00"), st.stBase());
        assertEquals(new BigDecimal("18.00"), st.stRate());
        assertEquals(new BigDecimal("22.68"), st.stAmount());
        assertEquals(new BigDecimal("126.00"), st.fcpStBase());
        assertEquals(new BigDecimal("2.00"), st.fcpStRate());
        assertEquals(new BigDecimal("2.52"), st.fcpStAmount());
    }

    @Test
    void shouldPreserveValuesExactlyAsSupplied() {
        IcmsStTax st = new IcmsStTax(
                "6", new BigDecimal("35.00"), null,
                new BigDecimal("135.00"), new BigDecimal("17.00"), new BigDecimal("22.95"),
                null, null, new BigDecimal("1.00"));

        assertEquals("6", st.modBcSt());
        assertEquals(new BigDecimal("35.00"), st.stMarginRate());
        assertNull(st.stReductionRate());
        assertEquals(new BigDecimal("135.00"), st.stBase());
        assertEquals(new BigDecimal("17.00"), st.stRate());
        assertEquals(new BigDecimal("22.95"), st.stAmount());
        assertNull(st.fcpStBase());
        assertNull(st.fcpStRate());
        assertEquals(new BigDecimal("1.00"), st.fcpStAmount());
    }
}
