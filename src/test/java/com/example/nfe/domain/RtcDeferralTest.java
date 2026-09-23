package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RtcDeferralTest {

    @Test
    void shouldAllowConstructionWithNullValues() {
        RtcDeferral deferral = new RtcDeferral(null, null);

        assertNull(deferral.rate());
        assertNull(deferral.amount());
    }

    @Test
    void shouldExposePopulatedValues() {
        RtcDeferral deferral = new RtcDeferral(new BigDecimal("10.00"), new BigDecimal("10.00"));

        assertEquals(new BigDecimal("10.00"), deferral.rate());
        assertEquals(new BigDecimal("10.00"), deferral.amount());
    }
}
