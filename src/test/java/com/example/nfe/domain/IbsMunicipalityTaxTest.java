package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class IbsMunicipalityTaxTest {

    @Test
    void shouldAllowConstructionWithAllNullFields() {
        IbsMunicipalityTax tax = new IbsMunicipalityTax(null, null, null, null, null);

        assertNull(tax.rate());
        assertNull(tax.deferral());
        assertNull(tax.devolution());
        assertNull(tax.reduction());
        assertNull(tax.amount());
    }

    @Test
    void shouldExposeRateAndAmountWithoutConditionalGroups() {
        IbsMunicipalityTax tax = new IbsMunicipalityTax(
                new BigDecimal("9.00"), null, null, null, new BigDecimal("9.00"));

        assertEquals(new BigDecimal("9.00"), tax.rate());
        assertEquals(new BigDecimal("9.00"), tax.amount());
        assertNull(tax.deferral());
        assertNull(tax.devolution());
        assertNull(tax.reduction());
    }

    @Test
    void shouldExposeConditionalGroups() {
        RtcDeferral deferral = new RtcDeferral(new BigDecimal("10.00"), new BigDecimal("10.00"));
        RtcDevolution devolution = new RtcDevolution(new BigDecimal("5.00"), new BigDecimal("5.00"));
        RtcReduction reduction = new RtcReduction(new BigDecimal("30.00"), new BigDecimal("6.30"));

        IbsMunicipalityTax tax = new IbsMunicipalityTax(
                new BigDecimal("9.00"), deferral, devolution, reduction, new BigDecimal("6.00"));

        assertEquals(new BigDecimal("9.00"), tax.rate());
        assertSame(deferral, tax.deferral());
        assertSame(devolution, tax.devolution());
        assertSame(reduction, tax.reduction());
        assertEquals(new BigDecimal("6.00"), tax.amount());
    }
}
