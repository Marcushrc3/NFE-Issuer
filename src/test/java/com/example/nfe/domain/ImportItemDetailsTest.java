package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ImportItemDetailsTest {

    @Test
    void shouldAllowConstructionWithAllNullValues() {
        ImportItemDetails details = new ImportItemDetails(null, null, null, null);

        assertNull(details.declarationNumber());
        assertNull(details.additionNumber());
        assertNull(details.additionSequence());
        assertNull(details.fci());
    }

    @Test
    void shouldAllowPartiallyNullValues() {
        ImportItemDetails details = new ImportItemDetails("24-1234567-8", null, null, null);

        assertEquals("24-1234567-8", details.declarationNumber());
        assertNull(details.additionNumber());
        assertNull(details.additionSequence());
        assertNull(details.fci());
    }

    @Test
    void shouldPreserveValuesExactlyAsSupplied() {
        ImportItemDetails details = new ImportItemDetails("24-1234567-8", 1, 2, null);

        assertEquals("24-1234567-8", details.declarationNumber());
        assertEquals(1, details.additionNumber());
        assertEquals(2, details.additionSequence());
    }

    @Test
    void shouldAllowNullFci() {
        ImportItemDetails details = new ImportItemDetails("24-1234567-8", 1, 2, null);

        assertEquals("24-1234567-8", details.declarationNumber());
        assertEquals(1, details.additionNumber());
        assertEquals(2, details.additionSequence());
        assertNull(details.fci());
    }

    @Test
    void shouldPreserveFciExactlyAsSupplied() {
        ImportItemDetails details = new ImportItemDetails("24-1234567-8", 1, 2, "12345678-9");

        assertEquals("12345678-9", details.fci());
    }

    @Test
    void shouldAllowFciWithoutChangingOtherImportFields() {
        ImportItemDetails details = new ImportItemDetails("24-1234567-8", 1, 2, "12345678-9");

        assertEquals("24-1234567-8", details.declarationNumber());
        assertEquals(1, details.additionNumber());
        assertEquals(2, details.additionSequence());
        assertEquals("12345678-9", details.fci());
    }

    @Test
    void shouldAllowImportItemDetailsWithOnlyFci() {
        ImportItemDetails details = new ImportItemDetails(null, null, null, "98765432-1");

        assertNull(details.declarationNumber());
        assertNull(details.additionNumber());
        assertNull(details.additionSequence());
        assertEquals("98765432-1", details.fci());
    }

    @Test
    void shouldKeepFciOptional() {
        ImportItemDetails details = new ImportItemDetails("24-1234567-8", 1, 2, null);

        assertNull(details.fci());
    }
}
