package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RtcReductionTest {

    @Test
    void shouldExposeReductionRateAndEffectiveRate() {
        RtcReduction reduction = new RtcReduction(new BigDecimal("30.00"), new BigDecimal("12.25"));

        assertEquals(new BigDecimal("30.00"), reduction.reductionRate());
        assertEquals(new BigDecimal("12.25"), reduction.effectiveRate());
    }
}
