package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AddressTest {

    @Test
    void shouldPreserveNeighborhoodAndMunicipalityCode() {
        Address address = new Address("Main St", "100", "Sao Paulo", "SP",
                "01310-100", "Centro", "3550308");

        assertEquals("Main St", address.street());
        assertEquals("100", address.number());
        assertEquals("Sao Paulo", address.city());
        assertEquals("SP", address.state());
        assertEquals("01310-100", address.zipCode());
        assertEquals("Centro", address.neighborhood());
        assertEquals("3550308", address.municipalityCode());
    }

    @Test
    void shouldAllowNullNeighborhoodAndMunicipalityCode() {
        Address address = new Address("Main St", "100", "Sao Paulo", "SP",
                "01310-100", null, null);

        assertNull(address.neighborhood());
        assertNull(address.municipalityCode());
    }

    @Test
    void shouldRejectMunicipalityCodeThatIsNotSevenDigits() {
        assertThrows(IllegalArgumentException.class,
                () -> new Address("Main St", "100", "Sao Paulo", "SP",
                        "01310-100", "Centro", "123"));
        assertThrows(IllegalArgumentException.class,
                () -> new Address("Main St", "100", "Sao Paulo", "SP",
                        "01310-100", "Centro", "355030X"));
    }
}
