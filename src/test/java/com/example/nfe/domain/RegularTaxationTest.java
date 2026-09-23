package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RegularTaxationTest {

    @Test
    void shouldAllowConstructionWithAllNullFields() {
        RegularTaxation regular = new RegularTaxation(null, null, null, null, null, null, null, null);

        assertNull(regular.cst());
        assertNull(regular.cClassTrib());
        assertNull(regular.ibsUfEffectiveRate());
        assertNull(regular.ibsUfTaxAmount());
        assertNull(regular.ibsMunicipalityEffectiveRate());
        assertNull(regular.ibsMunicipalityTaxAmount());
        assertNull(regular.cbsEffectiveRate());
        assertNull(regular.cbsTaxAmount());
    }

    @Test
    void shouldExposeAllStructuralFields() {
        RegularTaxation regular = new RegularTaxation(
                "800",
                "800000",
                new BigDecimal("17.50"),
                new BigDecimal("17.50"),
                new BigDecimal("9.00"),
                new BigDecimal("9.00"),
                new BigDecimal("8.80"),
                new BigDecimal("8.80"));

        assertEquals("800", regular.cst());
        assertEquals("800000", regular.cClassTrib());
        assertEquals(new BigDecimal("17.50"), regular.ibsUfEffectiveRate());
        assertEquals(new BigDecimal("17.50"), regular.ibsUfTaxAmount());
        assertEquals(new BigDecimal("9.00"), regular.ibsMunicipalityEffectiveRate());
        assertEquals(new BigDecimal("9.00"), regular.ibsMunicipalityTaxAmount());
        assertEquals(new BigDecimal("8.80"), regular.cbsEffectiveRate());
        assertEquals(new BigDecimal("8.80"), regular.cbsTaxAmount());
    }
}
