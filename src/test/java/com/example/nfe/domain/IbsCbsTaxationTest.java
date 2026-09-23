package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class IbsCbsTaxationTest {

    @Test
    void shouldAllowConstructionWithAllNullComponents() {
        IbsCbsTaxation taxation = new IbsCbsTaxation(null, null, null, null, null, null);

        assertNull(taxation.taxBase());
        assertNull(taxation.ibsUf());
        assertNull(taxation.ibsMunicipality());
        assertNull(taxation.ibsAmount());
        assertNull(taxation.cbs());
        assertNull(taxation.regularTaxation());
    }

    @Test
    void shouldExposeAllCoreFields() {
        IbsUfTax ibsUf = new IbsUfTax(
                new BigDecimal("17.50"), null, null, null, new BigDecimal("17.50"));
        IbsMunicipalityTax ibsMunicipality = new IbsMunicipalityTax(
                new BigDecimal("9.00"), null, null, null, new BigDecimal("9.00"));
        CbsTax cbs = new CbsTax(
                new BigDecimal("8.80"), null, null, null, new BigDecimal("8.80"));
        RegularTaxation regular = new RegularTaxation(
                "800", "800000",
                new BigDecimal("17.50"), new BigDecimal("17.50"),
                new BigDecimal("9.00"), new BigDecimal("9.00"),
                new BigDecimal("8.80"), new BigDecimal("8.80"));

        IbsCbsTaxation taxation = new IbsCbsTaxation(
                new BigDecimal("100.00"), ibsUf, ibsMunicipality,
                new BigDecimal("26.50"), cbs, regular);

        assertEquals(new BigDecimal("100.00"), taxation.taxBase());
        assertSame(ibsUf, taxation.ibsUf());
        assertSame(ibsMunicipality, taxation.ibsMunicipality());
        assertEquals(new BigDecimal("26.50"), taxation.ibsAmount());
        assertSame(cbs, taxation.cbs());
        assertSame(regular, taxation.regularTaxation());
    }

    @Test
    void shouldKeepIbsAmountIndependentFromJurisdictionAmounts() {
        IbsCbsTaxation taxation = new IbsCbsTaxation(
                new BigDecimal("100.00"),
                new IbsUfTax(new BigDecimal("17.50"), null, null, null, new BigDecimal("17.50")),
                new IbsMunicipalityTax(new BigDecimal("9.00"), null, null, null, new BigDecimal("9.00")),
                new BigDecimal("30.00"),
                null,
                null);

        assertEquals(new BigDecimal("30.00"), taxation.ibsAmount());
        assertEquals(new BigDecimal("17.50"), taxation.ibsUf().amount());
        assertEquals(new BigDecimal("9.00"), taxation.ibsMunicipality().amount());
    }
}
