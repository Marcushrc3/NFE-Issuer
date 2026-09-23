package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class RtcTaxationTest {

    @Test
    void shouldAllowConstructionWithAllNullComponents() {
        RtcTaxation rtc = new RtcTaxation(null, null, null, null);

        assertNull(rtc.cst());
        assertNull(rtc.cClassTrib());
        assertNull(rtc.indDoacao());
        assertNull(rtc.ibsCbs());
    }

    @Test
    void shouldExposeCstClassificationAndDonationIndicator() {
        RtcTaxation rtc = new RtcTaxation("100", "100000", "1", null);

        assertEquals("100", rtc.cst());
        assertEquals("100000", rtc.cClassTrib());
        assertEquals("1", rtc.indDoacao());
        assertNull(rtc.ibsCbs());
    }

    @Test
    void shouldAllowNullDonationIndicatorAndIbsCbs() {
        RtcTaxation rtc = new RtcTaxation("800", "800000", null, null);

        assertEquals("800", rtc.cst());
        assertEquals("800000", rtc.cClassTrib());
        assertNull(rtc.indDoacao());
        assertNull(rtc.ibsCbs());
    }

    @Test
    void shouldAllowConstructionWithPopulatedIbsCbs() {
        IbsCbsTaxation ibsCbs = new IbsCbsTaxation(
                new BigDecimal("100.00"),
                new IbsUfTax(new BigDecimal("17.50"), null, null, null, new BigDecimal("17.50")),
                new IbsMunicipalityTax(new BigDecimal("9.00"), null, null, null, new BigDecimal("9.00")),
                new BigDecimal("26.50"),
                new CbsTax(new BigDecimal("8.80"), null, null, null, new BigDecimal("8.80")),
                new RegularTaxation(
                        "800", "800000",
                        new BigDecimal("17.50"), new BigDecimal("17.50"),
                        new BigDecimal("9.00"), new BigDecimal("9.00"),
                        new BigDecimal("8.80"), new BigDecimal("8.80")));

        RtcTaxation rtc = new RtcTaxation("100", "100000", "1", ibsCbs);

        assertSame(ibsCbs, rtc.ibsCbs());
    }
}
