package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ImportTaxTest {

    @Test
    void shouldAllowConstructionWithAllNullValues() {
        ImportTax tax = new ImportTax(null, null, null, null, null);

        assertNull(tax.taxBase());
        assertNull(tax.taxRate());
        assertNull(tax.taxAmount());
        assertNull(tax.customsExpenses());
        assertNull(tax.iofAmount());
    }

    @Test
    void shouldAllowNullCustomsExpensesAndIof() {
        ImportTax tax = new ImportTax(
                new BigDecimal("1000.00"),
                new BigDecimal("10.00"),
                new BigDecimal("100.00"),
                null,
                null);

        assertEquals(new BigDecimal("1000.00"), tax.taxBase());
        assertEquals(new BigDecimal("10.00"), tax.taxRate());
        assertEquals(new BigDecimal("100.00"), tax.taxAmount());
        assertNull(tax.customsExpenses());
        assertNull(tax.iofAmount());
    }

    @Test
    void shouldPreserveCustomsExpensesAndIofExactlyAsSupplied() {
        ImportTax tax = new ImportTax(
                new BigDecimal("1000.00"),
                new BigDecimal("10.00"),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("3.00"));

        assertEquals(new BigDecimal("1000.00"), tax.taxBase());
        assertEquals(new BigDecimal("10.00"), tax.taxRate());
        assertEquals(new BigDecimal("100.00"), tax.taxAmount());
        assertEquals(new BigDecimal("50.00"), tax.customsExpenses());
        assertEquals(new BigDecimal("3.00"), tax.iofAmount());
    }
}
