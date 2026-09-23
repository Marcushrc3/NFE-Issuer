package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IsTaxTest {

    @Test
    void shouldAllowConstructionWithAllNullFields() {
        IsTax is = new IsTax(null, null, null, null, null, null, null, null);

        assertNull(is.cst());
        assertNull(is.cClassTrib());
        assertNull(is.taxBase());
        assertNull(is.rate());
        assertNull(is.adRemRate());
        assertNull(is.taxableUnit());
        assertNull(is.taxableQuantity());
        assertNull(is.amount());
    }

    @Test
    void shouldExposeAllStructuralFields() {
        IsTax is = new IsTax(
                "100",
                "100000",
                new BigDecimal("100.00"),
                new BigDecimal("1.00"),
                new BigDecimal("2.00"),
                "kg",
                new BigDecimal("10.0000"),
                new BigDecimal("20.00"));

        assertEquals("100", is.cst());
        assertEquals("100000", is.cClassTrib());
        assertEquals(new BigDecimal("100.00"), is.taxBase());
        assertEquals(new BigDecimal("1.00"), is.rate());
        assertEquals(new BigDecimal("2.00"), is.adRemRate());
        assertEquals("kg", is.taxableUnit());
        assertEquals(new BigDecimal("10.0000"), is.taxableQuantity());
        assertEquals(new BigDecimal("20.00"), is.amount());
    }

    @Test
    void shouldAllowOptionalFieldsToBeNull() {
        IsTax is = new IsTax(
                "300",
                "300000",
                new BigDecimal("50.00"),
                new BigDecimal("3.00"),
                null,
                null,
                null,
                new BigDecimal("1.50"));

        assertEquals("300", is.cst());
        assertEquals(new BigDecimal("1.50"), is.amount());
        assertNull(is.adRemRate());
        assertNull(is.taxableUnit());
        assertNull(is.taxableQuantity());
    }
}
