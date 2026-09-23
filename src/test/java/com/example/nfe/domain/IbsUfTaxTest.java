package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class IbsUfTaxTest {

    @Test
    void shouldAllowConstructionWithAllNullFields() {
        IbsUfTax tax = new IbsUfTax(null, null, null, null, null);

        assertNull(tax.rate());
        assertNull(tax.deferral());
        assertNull(tax.devolution());
        assertNull(tax.reduction());
        assertNull(tax.amount());
    }

    @Test
    void shouldExposeRateAndAmountWithoutConditionalGroups() {
        IbsUfTax tax = new IbsUfTax(
                new BigDecimal("17.50"), null, null, null, new BigDecimal("17.50"));

        assertEquals(new BigDecimal("17.50"), tax.rate());
        assertEquals(new BigDecimal("17.50"), tax.amount());
        assertNull(tax.deferral());
        assertNull(tax.devolution());
        assertNull(tax.reduction());
    }

    @Test
    void shouldExposeConditionalGroups() {
        RtcDeferral deferral = new RtcDeferral(new BigDecimal("10.00"), new BigDecimal("10.00"));
        RtcDevolution devolution = new RtcDevolution(new BigDecimal("5.00"), new BigDecimal("5.00"));
        RtcReduction reduction = new RtcReduction(new BigDecimal("30.00"), new BigDecimal("12.25"));

        IbsUfTax tax = new IbsUfTax(
                new BigDecimal("17.50"), deferral, devolution, reduction, new BigDecimal("10.00"));

        assertEquals(new BigDecimal("17.50"), tax.rate());
        assertSame(deferral, tax.deferral());
        assertSame(devolution, tax.devolution());
        assertSame(reduction, tax.reduction());
        assertEquals(new BigDecimal("10.00"), tax.amount());
    }
}
