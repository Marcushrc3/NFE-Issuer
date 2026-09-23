package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RtcDevolutionTest {

    @Test
    void shouldAllowNullRate() {
        RtcDevolution devolution = new RtcDevolution(null, new BigDecimal("5.00"));

        assertNull(devolution.rate());
        assertEquals(new BigDecimal("5.00"), devolution.amount());
    }

    @Test
    void shouldExposePopulatedRateAndAmount() {
        RtcDevolution devolution = new RtcDevolution(new BigDecimal("5.00"), new BigDecimal("5.00"));

        assertEquals(new BigDecimal("5.00"), devolution.rate());
        assertEquals(new BigDecimal("5.00"), devolution.amount());
    }

    @Test
    void shouldAllowConstructionWithAllNullValues() {
        RtcDevolution devolution = new RtcDevolution(null, null);

        assertNull(devolution.rate());
        assertNull(devolution.amount());
    }
}
