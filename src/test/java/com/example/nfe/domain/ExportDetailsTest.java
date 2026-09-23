package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExportDetailsTest {

    @Test
    void shouldAllowConstructionWithDestinationCountryOnly() {
        ExportDetails details = new ExportDetails("United States", null, null, null);

        assertEquals("United States", details.destinationCountry());
        assertNull(details.exitState());
        assertNull(details.exportDispatchLocation());
        assertNull(details.drawbackNumber());
    }

    @Test
    void shouldAllowConstructionWithAllFields() {
        ExportDetails details = new ExportDetails(
                "United States", "SP", "Porto de Santos", "123456789");

        assertEquals("United States", details.destinationCountry());
        assertEquals("SP", details.exitState());
        assertEquals("Porto de Santos", details.exportDispatchLocation());
        assertEquals("123456789", details.drawbackNumber());
    }

    @Test
    void shouldAllowAllFieldsToBeNull() {
        ExportDetails details = new ExportDetails(null, null, null, null);

        assertNull(details.destinationCountry());
        assertNull(details.exitState());
        assertNull(details.exportDispatchLocation());
        assertNull(details.drawbackNumber());
    }

    @Test
    void shouldPreserveValuesExactlyAsSupplied() {
        ExportDetails details = new ExportDetails(
                "Germany", "RJ", "Porto do Rio de Janeiro", "987");

        assertEquals("Germany", details.destinationCountry());
        assertEquals("RJ", details.exitState());
        assertEquals("Porto do Rio de Janeiro", details.exportDispatchLocation());
        assertEquals("987", details.drawbackNumber());
    }
}
