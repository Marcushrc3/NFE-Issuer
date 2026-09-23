package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CbsTaxTest {

    @Test
    void shouldAllowConstructionWithAllNullFields() {
        CbsTax cbs = new CbsTax(null, null, null, null, null);

        assertNull(cbs.rate());
        assertNull(cbs.deferral());
        assertNull(cbs.devolution());
        assertNull(cbs.reduction());
        assertNull(cbs.amount());
    }

    @Test
    void shouldExposeRateAndAmountWithoutConditionalGroups() {
        CbsTax cbs = new CbsTax(new BigDecimal("8.80"), null, null, null, new BigDecimal("8.80"));

        assertEquals(new BigDecimal("8.80"), cbs.rate());
        assertEquals(new BigDecimal("8.80"), cbs.amount());
        assertNull(cbs.deferral());
        assertNull(cbs.devolution());
        assertNull(cbs.reduction());
    }

    @Test
    void shouldExposeConditionalGroups() {
        RtcDeferral deferral = new RtcDeferral(new BigDecimal("10.00"), new BigDecimal("10.00"));
        RtcDevolution devolution = new RtcDevolution(new BigDecimal("5.00"), new BigDecimal("5.00"));
        RtcReduction reduction = new RtcReduction(new BigDecimal("30.00"), new BigDecimal("6.16"));

        CbsTax cbs = new CbsTax(
                new BigDecimal("8.80"), deferral, devolution, reduction, new BigDecimal("8.00"));

        assertEquals(new BigDecimal("8.80"), cbs.rate());
        assertSame(deferral, cbs.deferral());
        assertSame(devolution, cbs.devolution());
        assertSame(reduction, cbs.reduction());
        assertEquals(new BigDecimal("8.00"), cbs.amount());
    }
}
